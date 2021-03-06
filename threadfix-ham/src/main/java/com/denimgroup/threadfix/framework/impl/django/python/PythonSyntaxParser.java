////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.PythonTokenizerConfigurator;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.ScopeTracker;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.django.python.Language.PYTHON_KEYWORDS;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.*;

public class PythonSyntaxParser implements EventBasedTokenizer {

    private static SanitizedLogger LOG = new SanitizedLogger(PythonSyntaxParser.class);

    private static void log(String msg) {
        //LOG.info(msg);
        LOG.debug(msg);
    }

    private static boolean isModuleFolder(File folder) {
        return (new File(folder.getAbsolutePath() + File.separator + "__init__.py")).exists();
    }

    static String makeModuleName(File file) {
        String name = file.getName();
        if (name.contains("."))
            name = name.substring(0, name.lastIndexOf("."));
        name = name.replaceAll("\\-", "_");
        return name;
    }

    public static PythonCodeCollection run(File rootDirectory) {
        log("Running on " + rootDirectory.getAbsolutePath());
        PythonCodeCollection codebase = new PythonCodeCollection();
        // Use runRecursive when debugging, runParallel in release
        //runRecursive(rootDirectory, codebase);
        runParallel(rootDirectory, codebase);
        return codebase;
    }

    private static void runParallel(File rootDirectory, PythonCodeCollection codebase) {

        if (rootDirectory.isFile()) {
            runRecursive(rootDirectory, codebase);
            return;
        }

        Collection<File> pythonFiles = FileUtils.listFiles(rootDirectory, new String[] { "py"}, true);
        Map<PythonParallelParserRunner, Future<PythonModule>> pendingModules = map();

        log("Parsing " + pythonFiles.size() + " python files in parallel");

        int numThreads = Runtime.getRuntime().availableProcessors() / 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (File file : pythonFiles) {
            PythonParallelParserRunner runner = new PythonParallelParserRunner(file);
            pendingModules.put(runner, executor.submit(runner));
        }



        log("Finished queueing, waiting for futures...");

        Collection<PythonModule> finishedModules = list();

        int numCompleted = 0;
        for (Map.Entry<PythonParallelParserRunner, Future<PythonModule>> entry : pendingModules.entrySet()) {
            PythonParallelParserRunner runner = entry.getKey();
            Future<PythonModule> future = entry.getValue();
            try {
                log("Waiting for " + runner.getTargetFile().getAbsolutePath());
                PythonModule result = future.get();
                log("Finished " + ++numCompleted + "/" + pendingModules.size());
                finishedModules.add(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        log("Parsing tasks completed, reconstructing module hierarchy...");

        // Reconstruct directory-based hierarchy first
        List<File> folders = new ArrayList<File>(FileUtils.listFilesAndDirs(rootDirectory, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return false;
            }

            @Override
            public boolean accept(File file, String s) {
                return false;
            }
        }, new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        })
        );

        Collections.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return StringUtils.countMatches(o1.getAbsolutePath(), File.separator) - StringUtils.countMatches(o2.getAbsolutePath(), File.separator);
            }
        });

        // Start with folder modules

        for (File folder : folders) {
            String folderPath = folder.getAbsolutePath();
            if (folderPath.equals(rootDirectory.getAbsolutePath())) {
                continue;
            }

            String relativePath = folderPath.replace(rootDirectory.getAbsolutePath() + File.separator, "");
            if (relativePath.length() == 0 || relativePath.contains(".")) {
                continue;
            }
            PythonModule module = new PythonModule();
            module.setSourceCodePath(folder.getAbsolutePath());
            module.setName(makeModuleName(folder));
            if (!relativePath.contains(File.separator)) {
                codebase.add(module);
            } else {
                String modulePath = StringUtils.replace(relativePath, File.separator, ".");
                String basePath = modulePath.substring(0, modulePath.lastIndexOf('.'));
                PythonModule parentModule = codebase.findByFullName(basePath, PythonModule.class);
                if (parentModule != null) {
                    parentModule.addChildStatement(module);
                }
            }
        }

        // Now that the basic structure exists, start inserting parsed modules
        Queue<PythonModule> modulesQueue = new LinkedList<PythonModule>(finishedModules);
        while (!modulesQueue.isEmpty()) {
            PythonModule currentModule = modulesQueue.remove();
            String baseModule = currentModule.getSourceCodePath().replace(rootDirectory.getAbsolutePath() + File.separator, "");

            if (!baseModule.contains(File.separator)) {
                codebase.add(currentModule);
                continue;
            }

            // Remove file name
            baseModule = baseModule.substring(0, baseModule.lastIndexOf(File.separator));
            baseModule = StringUtils.replace(baseModule, File.separator, ".");

            PythonModule parentModule = codebase.findByFullName(baseModule, PythonModule.class);
            if (parentModule != null) {
                parentModule.addChildStatement(currentModule);
            }
        }

        log("Finished python syntax parsing");
    }

    private static void runRecursive(File rootDirectory, PythonCodeCollection codebase) {
        if (rootDirectory.isFile()) {
            PythonSyntaxParser parser = new PythonSyntaxParser(rootDirectory);
            EventBasedTokenizerRunner.run(rootDirectory, PythonTokenizerConfigurator.INSTANCE, parser);
            codebase.add(parser.getThisModule());
        } else {
            if (isModuleFolder(rootDirectory)) {
                PythonModule module = recurseCodeDirectory(rootDirectory);
                codebase.add(module);
            } else {
                for (File file : rootDirectory.listFiles()) {
                    if (file.isDirectory()) {
                        if (isModuleFolder(file)) {
                            PythonModule dirModule = recurseCodeDirectory(file);
                            if (dirModule != null) {
                                codebase.add(dirModule);
                            }
                        }
                    } else if (file.getName().endsWith(".py")) {
                        PythonSyntaxParser parser = new PythonSyntaxParser(file);
                        EventBasedTokenizerRunner.run(file, PythonTokenizerConfigurator.INSTANCE, parser);
                        codebase.add(parser.getThisModule());
                    }
                }
            }
        }
    }

    private static PythonModule recurseCodeDirectory(File rootDirectory) {
        File[] allFiles = rootDirectory.listFiles();
        PythonModule directoryModule = new PythonModule();
        directoryModule.setName(makeModuleName(rootDirectory));
        directoryModule.setSourceCodePath(rootDirectory.getAbsolutePath());
        for (File file : allFiles) {
            if (file.isFile()) {
                if (!file.getName().endsWith(".py")) {
                    continue;
                }

                log("Starting .py file " + file.getAbsolutePath());

                PythonSyntaxParser parser = new PythonSyntaxParser(file);
                EventBasedTokenizerRunner.run(file, PythonTokenizerConfigurator.INSTANCE, parser);

                directoryModule.addChildStatement(parser.getThisModule());

                log("Finished .py file " + file.getAbsolutePath());

            } else {
                if (isModuleFolder(file)) {
                    directoryModule.addChildStatement(recurseCodeDirectory(file));
                }
            }
        }

        return directoryModule;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    private PythonClass currentClass = null;
    private PythonFunction currentFunction = null;

    private String lastValidString;
    private int lastValidType;
    private String lastString;
    private int lastType = -1;
    private int numConsecutiveQuotes = 0;
    private boolean inMultilineString = false;

    private ScopeTracker scopeTracker = new ScopeTracker();

    private int spaceDepth = 0;
    private int classEntrySpaceDepth = -1;
    private int functionEntrySpaceDepth = -1;

    private PythonModule thisModule;
    private List<PythonClass> classes = list();
    private List<PythonFunction> globalFunctions = list();
    private List<PythonDecorator> pendingDecorators = list();
    private List<PythonPublicVariable> publicVariables = list();

    private List<AbstractPythonStatement> scopeStack = list();

    private boolean ranScopingCheck = false;

    private int lastLineNumber = -1;
    private boolean isComment = false;

    private boolean isInString() {
        return inMultilineString || scopeTracker.isInString();
    }


    private void pushScope(AbstractPythonStatement newScope) {
        scopeStack.add(newScope);
        if (newScope instanceof PythonClass) {
            currentClass = (PythonClass)newScope;
        } else if (newScope instanceof PythonFunction) {
            currentFunction = (PythonFunction)newScope;
        }
    }

    private void popScope() {
        if (scopeStack.size() == 0) {
            return;
        }

        AbstractPythonStatement lastEntry = scopeStack.get(scopeStack.size() - 1);
        Class entryType = lastEntry.getClass();
        scopeStack.remove(lastEntry);

        AbstractPythonStatement newCurrentEntry = null;
        ListIterator<AbstractPythonStatement> iterator = scopeStack.listIterator(scopeStack.size());
        while (iterator.hasPrevious()) {
            newCurrentEntry = iterator.previous();
            if (newCurrentEntry.getClass().isAssignableFrom(entryType)) {
                break;
            } else {
                newCurrentEntry = null;
            }
        }

        if (PythonClass.class.isAssignableFrom(entryType)) {
            if (newCurrentEntry == null) {
                currentClass = null;
            } else {
                currentClass = (PythonClass) newCurrentEntry;
            }
        } else if (PythonFunction.class.isAssignableFrom(entryType)) {
            if (newCurrentEntry == null) {
                currentFunction = null;
            } else {
                currentFunction = (PythonFunction)newCurrentEntry;
            }
        }
    }

    private AbstractPythonStatement getScope() {
        if (scopeStack.isEmpty()) {
            return thisModule;
        } else {
            return scopeStack.get(scopeStack.size() - 1);
        }
    }

    private void registerScopeOutput(AbstractPythonStatement scope) {

        AbstractPythonStatement currentScope = getScope();
        if (currentScope != null) {
            currentScope.addChildStatement(scope);
        } else {
            Class scopeClass = scope.getClass();
            if (PythonClass.class.isAssignableFrom(scopeClass)) {
                classes.add((PythonClass)scope);
            } else if (PythonFunction.class.isAssignableFrom(scopeClass)) {
                globalFunctions.add((PythonFunction)scope);
            } else if (PythonPublicVariable.class.isAssignableFrom(scopeClass)) {
                publicVariables.add((PythonPublicVariable)scope);
            }
        }

        log("Registered output " + scope.toString());
    }

    private void registerImport(String fullName, String alias) {
        importsMap.put(alias, fullName);
        if (thisModule != null) {
            AbstractPythonStatement currentScope = getScope();
            if (currentScope != null) {
                currentScope.addImport(fullName, alias);
            } else {
                thisModule.addImport(fullName, alias);
            }
        }
    }

    public PythonSyntaxParser() {
        thisModule = null;
    }

    public PythonSyntaxParser(PythonModule targetModule) {
        thisModule = targetModule;
    }

    public PythonSyntaxParser(File forFile) {
        String moduleName = makeModuleName(forFile);
        thisModule = new PythonModule();
        thisModule.setName(moduleName);
        thisModule.setSourceCodePath(forFile.getAbsolutePath());
    }

    // shortName, fullImportName
    Map<String, String> importsMap = map();

    private void attachImports(AbstractPythonStatement scope) {
        for (Map.Entry<String, String> importItem : importsMap.entrySet()) {
            scope.addImport(importItem.getValue(), importItem.getKey());
        }
    }

    private boolean isInClass() {
        return !scopeTracker.isInScopeOrString() &&
                spaceDepth > classEntrySpaceDepth && classEntrySpaceDepth >= 0;
    }

    private boolean isInMethod() {
        return !scopeTracker.isInScopeOrString() &&
                spaceDepth > functionEntrySpaceDepth && functionEntrySpaceDepth >= 0;
    }


    /**
     * @return The PythonModule constructed by this parser, if available. This will be null if no file parameters were
     * passed to the constructor.
     */
    public PythonModule getThisModule() {
        return thisModule;
    }

    enum ParsePhase {
        START,
        CLASS_NAME, CLASS_BASE_TYPES,
        DECL_FUNCTION_NAME, DECL_FUNCTION_PARAMS,
        INVOKE_FUNCTION_PARAMS,
        LAMBDA_PARAMS, LAMBDA_BODY,
        DECORATOR_NAME, DECORATOR_PARAMS,
        VARIABLE_ASSIGNMENT, VARIABLE_VALUE,
        FROM_IMPORT, IMPORT_AS
    }

    private ParsePhase parsePhase = ParsePhase.START;


    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '\n') {
            spaceDepth = 0;
            ranScopingCheck = false;
            isComment = false;
        }

        boolean isStringChar = (type == '\'' || type == '"');

        if (lastType == '\\' || (lastString != null && lastString.endsWith("\\"))) {
            isStringChar = false;
        }

        if (isStringChar && type == '"' && lastValidType == '"') {
            numConsecutiveQuotes++;
        } else {
            numConsecutiveQuotes = 0;
        }

        boolean exitedMultilineString = false;

        //  There were two consecutive quotes after a first one
        if (numConsecutiveQuotes == 2) {
            if (inMultilineString) {
                exitedMultilineString = true;
            }
            inMultilineString = !inMultilineString;
            numConsecutiveQuotes = 0;
        }

        if (type == '#' && !isInString()) {
            isComment = true;
        }

        if (lineNumber != lastLineNumber) {
            lastLineNumber = lineNumber;
        }

        if (isComment) {
            return;
        } else if (!exitedMultilineString && !inMultilineString) {
            if (type >= 0) {
                scopeTracker.interpretToken(type);
            }
            if (stringValue != null) {
                for (int i = 0; i < stringValue.length(); i++) {
                    scopeTracker.interpretToken((int)stringValue.charAt(i));
                }
                if (type >= 0) {
                    scopeTracker.interpretToken(type);
                }
            }
        }

        if (type == ' ') spaceDepth++;
        if (type == '\t') spaceDepth += 4;

        if (type == '@' && !isInMethod() && !isInString()) {
            parsePhase = DECORATOR_NAME;
        }


        if (!ranScopingCheck && !isStringChar && !isInString() && (stringValue != null || (type != ' ' && type != '\n' && type != '\t'))) {
            AbstractPythonStatement currentScope = getScope();
            while (currentScope != null && spaceDepth < currentScope.getIndentationLevel()) {
                currentScope.setSourceCodeEndLine(lineNumber - 1);
                popScope();
                currentScope = getScope();
            }
            ranScopingCheck = true;
        }

        switch (parsePhase) {
            case START:                  processStart                    (type, lineNumber, stringValue); break;
            case CLASS_NAME:             processClassName                (type, lineNumber, stringValue); break;
            case CLASS_BASE_TYPES:       processClassBaseTypes           (type, lineNumber, stringValue); break;
            case DECL_FUNCTION_NAME:     processFunctionDeclarationName  (type, lineNumber, stringValue); break;
            case DECL_FUNCTION_PARAMS:   processFunctionDeclarationParams(type, lineNumber, stringValue); break;
            case INVOKE_FUNCTION_PARAMS: processInvokeFunctionParams     (type, lineNumber, stringValue); break;
            case LAMBDA_PARAMS:          processLambdaParams             (type, lineNumber, stringValue); break;
            case LAMBDA_BODY:            processLambdaBody               (type, lineNumber, stringValue); break;
            case DECORATOR_NAME:         processDecoratorName            (type, lineNumber, stringValue); break;
            case DECORATOR_PARAMS:       processDecoratorParams          (type, lineNumber, stringValue); break;
            case VARIABLE_ASSIGNMENT:    processVariableAssignment       (type, lineNumber, stringValue); break;
            case VARIABLE_VALUE:         processVariableValue            (type, lineNumber, stringValue); break;
            case FROM_IMPORT:            processFromImport               (type, lineNumber, stringValue); break;
            case IMPORT_AS:              processImportAs                 (type, lineNumber, stringValue); break;
        }

        if (stringValue != null) lastValidString = stringValue;
        if (type > 0) lastValidType = type;
        lastString = stringValue;
        lastType = type;
    }

    private void processStart(int type, int lineNumber, String stringValue) {
        if (stringValue != null && !isInString()) {
            if (stringValue.equals("class")) {
                parsePhase = ParsePhase.CLASS_NAME;
            } else if (stringValue.equals("def")) {
                parsePhase = ParsePhase.DECL_FUNCTION_NAME;
            } else if (!isInMethod() && !isInClass() && scopeTracker.getNumOpenParen() == 0 && !isInString()) {
                if (stringValue.equals("from")) {
                    parsePhase = FROM_IMPORT;
                } else if (stringValue.equals("import")) {
                    parsePhase = IMPORT_AS;
                }
            }
        } else if ((type == '=' || type == '-' || type == '+') && !isInMethod() && scopeTracker.getNumOpenParen() == 0 && !isInString()) {
            parsePhase = VARIABLE_ASSIGNMENT;
        } else if (type == '(' && !isInString()) {
            parsePhase = INVOKE_FUNCTION_PARAMS;
        }
    }

    private void processClassName(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            currentClass = new PythonClass();
            currentClass.setName(stringValue);
            currentClass.setSourceCodeStartLine(lineNumber);
            currentClass.setSourceCodePath(this.thisModule.getSourceCodePath());
            currentClass.setIndentationLevel(spaceDepth);
            classEntrySpaceDepth = spaceDepth;

            for (PythonDecorator decorator : pendingDecorators) {
                currentClass.addDecorator(decorator);
            }
            pendingDecorators.clear();

            parsePhase = ParsePhase.CLASS_BASE_TYPES;
        }
    }

    private void processClassBaseTypes(int type, int lineNumber, String stringValue) {
        if (scopeTracker.getNumOpenParen() == 0) {
            attachImports(currentClass);
            registerScopeOutput(currentClass);
            pushScope(currentClass);
            classEntrySpaceDepth = -1;
            parsePhase = ParsePhase.START;
        } else if (stringValue != null) {
            currentClass.addBaseType(stringValue);
        }
    }

    private void processFunctionDeclarationName(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            functionEntrySpaceDepth = spaceDepth;
            PythonFunction newFunction = new PythonFunction();

            if (isInClass()) {
                newFunction.setParentStatement(currentClass);
            } else if (isInMethod()) {
                currentFunction.addChildStatement(newFunction);
            } else {
                newFunction = new PythonFunction();
            }

            newFunction.setName(stringValue);
            newFunction.setSourceCodeStartLine(lineNumber);
            newFunction.setSourceCodePath(this.thisModule.getSourceCodePath());
            newFunction.setIndentationLevel(spaceDepth);

            for (PythonDecorator decorator : pendingDecorators) {
                newFunction.addDecorator(decorator);
            }

            pendingDecorators.clear();

            currentFunction = newFunction;
            parsePhase = ParsePhase.DECL_FUNCTION_PARAMS;
        }
    }

    private void processFunctionDeclarationParams(int type, int lineNumber, String stringValue) {
        if (scopeTracker.getNumOpenParen() == 0) {
            if (currentFunction.getOwnerClass() == null && currentFunction.getOwnerFunction() == null) {
                //attachImports(currentFunction);
                registerScopeOutput(currentFunction);
            }
            pushScope(currentFunction);
            functionEntrySpaceDepth = -1;
            parsePhase = ParsePhase.START;
        } else if (stringValue != null) {
            currentFunction.addParam(stringValue);
        }
    }

    PythonFunctionCall workingFunctionCall = null;
    StringBuilder workingFunctionCallParams = null;
    int functionCallStartNumParen = -1;
    private void processInvokeFunctionParams(int type, int lineNumber, String stringValue) {

        if (!isInString() && stringValue != null &&
                (PYTHON_KEYWORDS.contains(stringValue) || PYTHON_KEYWORDS.contains(lastValidString))) {
            parsePhase = START;
            return;
        }

        if (workingFunctionCall == null) {
            workingFunctionCall = new PythonFunctionCall();
            workingFunctionCallParams = new StringBuilder();
            workingFunctionCall.setSourceCodeStartLine(lineNumber);
            workingFunctionCall.setSourceCodePath(this.getThisModule().getSourceCodePath());
            workingFunctionCall.setIndentationLevel(spaceDepth);

            functionCallStartNumParen = scopeTracker.getNumOpenParen();

            String callName = lastValidString;
            if (callName.contains(".")) {
                String callee = callName.substring(0, callName.lastIndexOf('.'));
                String functionName = callName.substring(callName.lastIndexOf('.') + 1);
                workingFunctionCall.setCall(callee, functionName);
            } else {
                workingFunctionCall.setCall(callName);
            }
        }

        if (scopeTracker.getNumOpenParen() < functionCallStartNumParen || scopeTracker.getNumOpenParen() == 0) {
            String[] params = CodeParseUtil.splitByComma(workingFunctionCallParams.toString());
            workingFunctionCall.setParameters(Arrays.asList(params));
            registerScopeOutput(workingFunctionCall);
            workingFunctionCall = null;
            workingFunctionCallParams = null;
            parsePhase = START;
        } else {
            workingFunctionCallParams.append(CodeParseUtil.buildTokenString(type, stringValue));
        }
    }

    PythonLambda workingLambda = null;
    int lambda_startNumOpenParen;
    int lambda_startNumOpenBrace;
    int lambda_startNumOpenBracket;

    private void processLambdaParams(int type, int lineNumber, String stringValue) {
        boolean endsParams = false;

        if (type == ':') {
            endsParams = true;
        }

        if (stringValue != null) {
            if (stringValue.endsWith(":")) {
                endsParams = true;
                stringValue = stringValue.substring(0, stringValue.length() - 1);
            }
            workingLambda.addParam(stringValue);
        }

        if (endsParams) {
            lambda_startNumOpenParen = scopeTracker.getNumOpenParen();
            lambda_startNumOpenBrace = scopeTracker.getNumOpenBrace();
            lambda_startNumOpenBracket = scopeTracker.getNumOpenBracket();
            parsePhase = LAMBDA_BODY;
        }
    }

    StringBuilder workingLambdaBody = null;
    private void processLambdaBody(int type, int lineNumber, String stringValue) {
        if (workingLambdaBody == null) {
            workingLambdaBody = new StringBuilder();
        }

        if (type == '\n' && lambda_startNumOpenBrace <= scopeTracker.getNumOpenParen() &&
                lambda_startNumOpenBracket == scopeTracker.getNumOpenBracket() && lambda_startNumOpenBrace == scopeTracker.getNumOpenBrace()) {

            workingLambda.setFunctionBody(workingLambdaBody.toString());
            workingLambda.setSourceCodeEndLine(lineNumber - 1);

            registerScopeOutput(workingLambda);

            workingLambda = null;
            workingLambdaBody = null;
            lambda_startNumOpenBrace = -1;
            lambda_startNumOpenBracket = -1;
            lambda_startNumOpenParen = -1;
            parsePhase = START;
        } else {
            workingLambdaBody.append(CodeParseUtil.buildTokenString(type, stringValue));
        }

    }

    PythonDecorator currentDecorator;
    StringBuilder workingDecoratorParam;
    int decorator_startParenIndex = -1;
    int decorator_startBraceIndex = -1;
    int decorator_startBracketIndex = -1;

    private void processDecoratorName(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            currentDecorator = new PythonDecorator();
            currentDecorator.setName(stringValue);
            parsePhase = DECORATOR_PARAMS;
            decorator_startParenIndex = scopeTracker.getNumOpenParen();
            decorator_startBraceIndex = scopeTracker.getNumOpenBrace();
            decorator_startBracketIndex = scopeTracker.getNumOpenBracket();
            workingDecoratorParam = new StringBuilder();
        }
    }

    private void processDecoratorParams(int type, int lineNumber, String stringValue) {
        if (scopeTracker.getNumOpenParen() == 0) {
            if (workingDecoratorParam != null && workingDecoratorParam.length() > 0) {
                String paramValue = cleanParamValue(workingDecoratorParam.toString());
                currentDecorator.addParam(paramValue);
            }
            workingDecoratorParam = null;
            pendingDecorators.add(currentDecorator);
            parsePhase = START;
        } else {
            if (type == ',' && scopeTracker.getNumOpenParen() != decorator_startParenIndex &&
                    scopeTracker.getNumOpenBrace() != decorator_startBraceIndex &&
                    scopeTracker.getNumOpenBracket() != decorator_startBracketIndex) {

                String paramValue = cleanParamValue(workingDecoratorParam.toString());
                currentDecorator.addParam(paramValue);
                workingDecoratorParam = new StringBuilder();
            } else {
                workingDecoratorParam.append(CodeParseUtil.buildTokenString(type, stringValue));
            }
        }
    }

    PythonVariableModification workingVarChange;
    StringBuilder workingVarValue = null;
    int initialOperatorType = -1;
    private void processVariableAssignment(int type, int lineNumber, String stringValue) {
        if (workingVarChange == null) {
            workingVarChange = new PythonVariableModification();
            workingVarChange.setSourceCodeStartLine(lineNumber);
            workingVarChange.setSourceCodePath(this.thisModule.getSourceCodePath());
            workingVarChange.setTarget(lastValidString);
            workingVarChange.setIndentationLevel(spaceDepth);
            initialOperatorType = lastValidType;
        }

        if (stringValue != null) {
            workingVarValue = new StringBuilder(CodeParseUtil.buildTokenString(type, stringValue));
        } else {
            workingVarValue = new StringBuilder();
        }

        if (workingVarChange.getModificationType() == VariableModificationType.UNKNOWN) {
            if (initialOperatorType == '=' && type != '=') {
                workingVarChange.setModificationType(VariableModificationType.ASSIGNMENT);
            } else if (initialOperatorType == '+' && type == '=') {
                workingVarChange.setModificationType(VariableModificationType.CONCATENATION);
            } else if (initialOperatorType == '-' && type == '=') {
                workingVarChange.setModificationType(VariableModificationType.REMOVAL);
            } else {
                workingVarChange = null;
                initialOperatorType = -1;
                parsePhase = START;
                return;
            }
        }

        parsePhase = VARIABLE_VALUE;
    }

    private void processVariableValue(int type, int lineNumber, String stringValue) {
        if (type == '\n' && scopeTracker.getNumOpenParen() == 0 && scopeTracker.getNumOpenBrace() == 0 && scopeTracker.getNumOpenBracket() == 0) {

            String varValue = workingVarValue.toString();

            workingVarChange.setOperatorValue(varValue);
            workingVarChange.setSourceCodeEndLine(lineNumber - 1);
            String varName = workingVarChange.getTarget();

            if (getScope().findChild(varName) == null) {
                PythonPublicVariable variable = new PythonPublicVariable();
                variable.setSourceCodeStartLine(workingVarChange.getSourceCodeStartLine());
                variable.setSourceCodeEndLine(workingVarChange.getSourceCodeEndLine());
                variable.setSourceCodePath(workingVarChange.getSourceCodePath());
                variable.setName(varName);
                variable.setValueString(varValue);
                variable.setIndentationLevel(workingVarChange.getIndentationLevel());
                registerScopeOutput(variable);
            }

            registerScopeOutput(workingVarChange);
            initialOperatorType = -1;
            workingVarChange = null;
            workingVarValue = null;
            parsePhase = START;
        } else if (stringValue != null && stringValue.equals("lambda") && workingVarValue.length() == 0) {

            if (workingLambda == null) {
                workingLambda = new PythonLambda();
                workingLambda.setIndentationLevel(spaceDepth);
                workingLambda.setSourceCodeStartLine(lineNumber);
                workingLambda.setSourceCodePath(thisModule.getSourceCodePath());
            }

            workingLambda.setName(workingVarChange.getTarget());

            parsePhase = LAMBDA_PARAMS;
            workingVarValue = null;
            workingVarChange = null;
            initialOperatorType = -1;
        } else {
            workingVarValue.append(CodeParseUtil.buildTokenString(type, stringValue));
        }
    }

    String importName; // Package being pulled from
    String importNameWithItem; // Full name of the object being imported (including package names)
    String importItem; // Name of the input item, either an alias or the original import name

    private void processFromImport(int type, int lineNumber, String stringValue) {

        if (type == '\n' && scopeTracker.getNumOpenParen() == 0) {

            if (importName != null) {
                //  If a package was specified without an import target
                if (importNameWithItem == null) {
                    importNameWithItem = importName;
                    importItem = importName;
                }
                registerImport(importNameWithItem, importItem);
            }

            importNameWithItem = null;
            importItem = null;
            importName = null;
            parsePhase = START;
            return;
        }

        boolean isImportPath = true;
        if (type == ' ' || type == '(' || type == ')' || type == '\t') {
            isImportPath = false;
        }

        if (stringValue != null) {
            if (stringValue.equals("import")) {
                isImportPath = false;

            } else if (stringValue.equals("as")) {
                isImportPath = false;
                importItem = null;
            }
        }

        if (type == ',') {
            if (importName != null && importNameWithItem == null) {
                importNameWithItem = importName;
                importItem = importName;
            }
            isImportPath = false;
            registerImport(importNameWithItem, importItem);
            importItem = null;
            importNameWithItem = null;
        }

        if (isImportPath) {
            if (importName == null) {
                importName = CodeParseUtil.buildTokenString(type, stringValue);
            } else if (importItem == null) {
                importItem = CodeParseUtil.buildTokenString(type, stringValue);
                if (importNameWithItem == null) {
                    if (importName.endsWith(".") || importItem.startsWith(".")) {
                        importNameWithItem = importName + importItem;
                    } else {
                        importNameWithItem = importName + "." + importItem;
                    }
                }
            }
        }
    }

    private void processImportAs(int type, int lineNumber, String stringValue) {

        if (type == '\n' && scopeTracker.getNumOpenParen() == 0) {
            if (importName != null && importNameWithItem == null) {
                importNameWithItem = importName;
                importItem = importName;
            }
            registerImport(importNameWithItem, importItem);
            importNameWithItem = null;
            importItem = null;
            importName = null;
            parsePhase = START;
            return;
        }

        boolean isImportPath = true;
        if (type == ' ' || type == '(' || type == ')' || type == '\t') {
            isImportPath = false;
        }

        if (stringValue != null) {
            if (stringValue.equals("import")) {
                isImportPath = false;

            } else if (stringValue.equals("as")) {
                isImportPath = false;
                importItem = null;
            }
        }

        if (type == ',') {
            isImportPath = false;
            if (importName != null && importNameWithItem == null) {
                importNameWithItem = importName;
                importItem = importName;
            }
            registerImport(importNameWithItem, importItem);
            importItem = null;
            importNameWithItem = null;
        }

        if (isImportPath) {
            if (importName == null) {
                importName = CodeParseUtil.buildTokenString(type, stringValue);
            } else if (importItem == null) {
                importItem = CodeParseUtil.buildTokenString(type, stringValue);
                if (importNameWithItem == null) {
                    if (importName.endsWith(".") || importItem.startsWith(".")) {
                        importNameWithItem = importName + importItem;
                    } else {
                        importNameWithItem = importName + "." + importItem;
                    }
                }
            }
        }
    }

    private String cleanParamValue(String value) {
        if (value.startsWith("(")) {
            value = value.substring(1);
            if (value.endsWith(")")) {
                value = value.substring(0, value.length() - 1);
            }
        }
        value = value.trim();
        return value;
    }
}
