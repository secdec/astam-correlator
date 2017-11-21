package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.PythonTokenizerConfigurator;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.*;

public class PythonSyntaxParser implements EventBasedTokenizer {

    private static SanitizedLogger LOG = new SanitizedLogger(PythonSyntaxParser.class);

    private static void log(String msg) {
        //LOG.info(msg);
        LOG.debug(msg);
    }

    private static boolean isModuleFolder(File folder) {
        return (new File(folder.getAbsolutePath() + "/__init__.py")).exists();
    }

    private static String makeModuleName(File file) {
        String name = file.getName();
        if (name.contains("."))
            name = name.substring(0, name.lastIndexOf("."));
        name = name.replaceAll("\\-", "_");
        return name;
    }

    public static PythonCodeCollection run(File rootDirectory) {
        log("Running on " + rootDirectory.getAbsolutePath());
        PythonCodeCollection codebase = new PythonCodeCollection();
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

        codebase.expandImports();
        return codebase;
    }

    public static PythonModule run(String pythonCode) {
        log("Running on python code string: " + pythonCode);
        PythonModule result = new PythonModule();
        PythonSyntaxParser parser = new PythonSyntaxParser(result);

        EventBasedTokenizerRunner.runString(pythonCode, PythonTokenizerConfigurator.INSTANCE, parser);

        return parser.getThisModule();
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

    PythonClass currentClass = null;
    PythonFunction currentFunction = null;

    String lastValidString;
    int lastValidType;
    String lastString;
    int lastType = -1;
    boolean isInString = false;

    int numOpenParen = 0;
    int numOpenBrace = 0;
    int numOpenBracket = 0;

    int spaceDepth = 0;
    int classEntrySpaceDepth = -1;
    int functionEntrySpaceDepth = -1;

    PythonModule thisModule;
    List<PythonClass> classes = list();
    List<PythonFunction> globalFunctions = list();
    List<PythonDecorator> pendingDecorators = list();
    List<PythonPublicVariable> publicVariables = list();

    List<AbstractPythonStatement> scopeStack = list();

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

        if (thisModule != null) {
            thisModule.addChildStatement(scope);
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

    boolean isInClass() {
        return numOpenBrace == 0 && numOpenParen == 0 &&
                spaceDepth > classEntrySpaceDepth && classEntrySpaceDepth >= 0;
    }

    boolean isInMethod() {
        return numOpenBrace == 0 && numOpenParen == 0 &&
                spaceDepth > functionEntrySpaceDepth && functionEntrySpaceDepth >= 0;
    }

    public Collection<PythonClass> getClasses() {
        return classes;
    }

    public Collection<PythonFunction> getGlobalFunctions() {
        return globalFunctions;
    }

    public Collection<PythonPublicVariable> getPublicVariables() { return publicVariables; }


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
        DECORATOR_NAME, DECORATOR_PARAMS,
        VARIABLE_ASSIGNMENT, VARIABLE_VALUE,
        FROM_IMPORT, IMPORT_AS
    }
    private ParsePhase parsePhase = ParsePhase.START;

    private boolean isModifyingUrlPatterns = false;
    private int urlPatterns_numStartOpenParen = -1,
                urlPatterns_numStartOpenBrace = -1,
                urlPatterns_numStartOpenBracket = -1;

    boolean ranScopingCheck = false;

    int lastLineNumber = -1;
    int quoteStartType = -1;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (!isInString) {
            if (type == '(') numOpenParen++;
            if (type == ')') numOpenParen--;
            if (type == '{') numOpenBrace++;
            if (type == '}') numOpenBrace--;
            if (type == '[') numOpenBracket++;
            if (type == ']') numOpenBracket--;
        }

        if (stringValue != null && stringValue.equals("):")) {
            numOpenParen--;
        }

        if (type == '\n') {
            spaceDepth = 0;
            ranScopingCheck = false;
        }

        if (lineNumber != lastLineNumber) {
            lastLineNumber = lineNumber;
        }

        if (type == ' ') spaceDepth++;
        if (type == '\t') spaceDepth += 4;

        boolean isStringChar = (type == '\'' || type == '"');

        if (lastType == '\\' || (lastString != null && lastString.endsWith("\\"))) {
            isStringChar = false;
        }

        if (isStringChar && stringValue == null) {
            if (quoteStartType < 0) {
                isInString = !isInString;
                quoteStartType = type;
            } else {
                if (quoteStartType == type) {
                    quoteStartType = -1;
                    isInString = !isInString;
                }
            }
        }

        if (type == '@' && !isInMethod() && !isInString) {
            parsePhase = DECORATOR_NAME;
        }

        if (!isInString && stringValue != null && (stringValue.equals("urlpatterns") || stringValue.equals("urls"))) {
            isModifyingUrlPatterns = true;
        }

        if (!ranScopingCheck && !isStringChar && !isInString && (stringValue != null || (type != ' ' && type != '\n' && type != '\t'))) {
            AbstractPythonStatement currentScope = getScope();
            while (currentScope != null && spaceDepth < currentScope.getIndentationLevel()) {
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
        if (stringValue != null && !isInString) {
            if (stringValue.equals("class")) {
                parsePhase = ParsePhase.CLASS_NAME;
            } else if (stringValue.equals("def")) {
                parsePhase = ParsePhase.DECL_FUNCTION_NAME;
            } else if (!isInMethod() && !isInClass() && numOpenParen == 0 && !isInString) {
                if (stringValue.equals("from")) {
                    parsePhase = FROM_IMPORT;
                } else if (stringValue.equals("import")) {
                    parsePhase = IMPORT_AS;
                }
            }
        } else if ((type == '=' || type == '-' || type == '+') && !isInMethod() && numOpenParen == 0 && !isInString) {
            parsePhase = VARIABLE_ASSIGNMENT;
        } else if (type == '(' && !isInString) {
            parsePhase = INVOKE_FUNCTION_PARAMS;
        }
    }

    private void processClassName(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            currentClass = new PythonClass();
            currentClass.setName(stringValue);
            currentClass.setSourceCodeLine(lineNumber);
            currentClass.setSourceCodePath(this.thisModule.getSourceCodePath());
            currentClass.setIndentationLevel(spaceDepth);
            classEntrySpaceDepth = spaceDepth;

            for (PythonDecorator decorator : pendingDecorators) {
                currentClass.addDecorator(decorator);
            }
            pendingDecorators.clear();

            parsePhase = ParsePhase.CLASS_BASE_TYPES;

            pushScope(currentClass);
        }
    }

    private void processClassBaseTypes(int type, int lineNumber, String stringValue) {
        if (numOpenParen == 0) {
            attachImports(currentClass);
            registerScopeOutput(currentClass);
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
            newFunction.setSourceCodeLine(lineNumber);
            newFunction.setSourceCodePath(this.thisModule.getSourceCodePath());
            newFunction.setIndentationLevel(spaceDepth);

            pushScope(newFunction);

            for (PythonDecorator decorator : pendingDecorators) {
                newFunction.addDecorator(decorator);
            }

            pendingDecorators.clear();

            parsePhase = ParsePhase.DECL_FUNCTION_PARAMS;
        }
    }

    private void processFunctionDeclarationParams(int type, int lineNumber, String stringValue) {
        if (numOpenParen == 0) {
            if (currentFunction.getOwnerClass() == null && currentFunction.getOwnerFunction() == null) {
                //attachImports(currentFunction);
                registerScopeOutput(currentFunction);
            }
            functionEntrySpaceDepth = -1;
            parsePhase = ParsePhase.START;
        } else if (stringValue != null) {
            currentFunction.addParam(stringValue);
        }
    }

    PythonFunctionCall workingFunctionCall = null;
    String workingFunctionCallParams = null;
    private void processInvokeFunctionParams(int type, int lineNumber, String stringValue) {
        if (workingFunctionCall == null) {
            workingFunctionCall = new PythonFunctionCall();
            workingFunctionCallParams = "";

            String callName = lastValidString;
            if (callName.contains(".")) {
                String callee = callName.substring(0, callName.lastIndexOf('.'));
                String functionName = callName.substring(callName.lastIndexOf('.') + 1);
                workingFunctionCall.setCall(callee, functionName);
            } else {
                workingFunctionCall.setCall(callName);
            }
        }

        if (numOpenParen == 0) {
            String[] params = CodeParseUtil.splitByComma(workingFunctionCallParams);
            workingFunctionCall.setParameters(Arrays.asList(params));
            registerScopeOutput(workingFunctionCall);
            workingFunctionCall = null;
            workingFunctionCallParams = null;
            parsePhase = START;
        } else {
            workingFunctionCallParams += CodeParseUtil.buildTokenString(type, stringValue);
        }
    }

    PythonDecorator currentDecorator;
    String workingDecoratorParam;
    int decorator_startParenIndex = -1;
    int decorator_startBraceIndex = -1;
    int decorator_startBracketIndex = -1;

    private void processDecoratorName(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            currentDecorator = new PythonDecorator();
            currentDecorator.setName(stringValue);
            parsePhase = DECORATOR_PARAMS;
            decorator_startParenIndex = numOpenParen;
            decorator_startBraceIndex = numOpenBrace;
            decorator_startBracketIndex = numOpenBrace;
            workingDecoratorParam = "";
        }
    }

    private void processDecoratorParams(int type, int lineNumber, String stringValue) {
        if (numOpenParen == 0) {
            if (workingDecoratorParam != null && workingDecoratorParam.length() > 0) {
                workingDecoratorParam = cleanParamValue(workingDecoratorParam);
                currentDecorator.addParam(workingDecoratorParam);
            }
            pendingDecorators.add(currentDecorator);
            parsePhase = START;
        } else {
            if (type == ',' && numOpenParen != decorator_startParenIndex &&
                    numOpenBrace != decorator_startBraceIndex &&
                    numOpenBracket != decorator_startBracketIndex) {

                workingDecoratorParam = cleanParamValue(workingDecoratorParam);
                currentDecorator.addParam(workingDecoratorParam);
                workingDecoratorParam = "";

            } else {
                workingDecoratorParam += CodeParseUtil.buildTokenString(type, stringValue);
            }
        }
    }

    PythonVariableModification workingVarChange;
    String workingVarValue = null;
    int initialOperatorType = -1;
    VariableModificationType variableChangeType = VariableModificationType.UNKNOWN;
    private void processVariableAssignment(int type, int lineNumber, String stringValue) {
        if (workingVarChange == null) {
            workingVarChange = new PythonVariableModification();
            workingVarChange.setSourceCodeLine(lineNumber);
            workingVarChange.setTarget(lastValidString);
            workingVarChange.setSourceCodePath(this.thisModule.getSourceCodePath());
            initialOperatorType = lastValidType;
        }

        if (stringValue != null) {
            workingVarValue = CodeParseUtil.buildTokenString(type, stringValue);
        } else {
            workingVarValue = "";
        }

        if (variableChangeType == VariableModificationType.UNKNOWN) {
            if (initialOperatorType == '=') {
                variableChangeType = VariableModificationType.ASSIGNMENT;
            } else if (initialOperatorType == '+' && type == '=') {
                variableChangeType = VariableModificationType.CONCATENATION;
            } else if (initialOperatorType == '-' && type == '=') {
                variableChangeType = VariableModificationType.REMOVAL;
            }
        }

        parsePhase = VARIABLE_VALUE;
    }

    private void processVariableValue(int type, int lineNumber, String stringValue) {
        if (type == '\n' && numOpenParen == 0 && numOpenBrace == 0 && numOpenBracket == 0) {

            workingVarChange.setOperatorValue(workingVarValue);
            String varName = workingVarChange.getTarget();

            if (getScope().findChild(varName) == null) {
                PythonPublicVariable variable = new PythonPublicVariable();
                variable.setName(varName);
                variable.setValueString(workingVarValue);
                registerScopeOutput(variable);
            }

            registerScopeOutput(workingVarChange);

            variableChangeType = VariableModificationType.UNKNOWN;
            initialOperatorType = -1;
            workingVarChange = null;
            workingVarValue = null;
            parsePhase = START;
        } else {
            workingVarValue += CodeParseUtil.buildTokenString(type, stringValue);
        }
    }

    String importName; // Package being pulled from
    String importNameWithItem; // Full name of the object being imported (including package names)
    String importItem; // Name of the input item, either an alias or the original import name

    private void processFromImport(int type, int lineNumber, String stringValue) {

        if (type == '\n' && numOpenParen == 0) {

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

        if (type == '\n' && numOpenParen == 0) {
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

    private boolean hasParsedVariable(String fullVariableName) {
        for (PythonPublicVariable var : publicVariables) {
            if (var.getFullName().equals(fullVariableName)) {
                return true;
            }
        }
        return false;
    }

    private String cleanParamValue(String value) {
        if (value.startsWith("(")) {
            value = value.substring(1);
            if (value.endsWith(")")) {
                value = value.substring(0, value.length() - 1);
            }
        }
        return value;
    }
}
