package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.DjangoTokenizerConfigurator;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
            EventBasedTokenizerRunner.run(rootDirectory, DjangoTokenizerConfigurator.INSTANCE, parser);
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
                        EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);
                        codebase.add(parser.getThisModule());
                    }
                }
            }
        }

        codebase.expandImports();
        return codebase;
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
                EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);

                directoryModule.addChildScope(parser.getThisModule());

                log("Finished .py file " + file.getAbsolutePath());

            } else {
                if (isModuleFolder(file)) {
                    directoryModule.addChildScope(recurseCodeDirectory(file));
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

    String lastString;
    int lastType;
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

    private void registerScopeOutput(AbstractPythonScope scope) {

        if (thisModule != null) {
            thisModule.addChildScope(scope);
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
            thisModule.addImport(fullName, alias);
        }
    }

    public PythonSyntaxParser() {
        thisModule = null;
    }

    public PythonSyntaxParser(File forFile) {
        String moduleName = makeModuleName(forFile);
        thisModule = new PythonModule();
        thisModule.setName(moduleName);
        thisModule.setSourceCodePath(forFile.getAbsolutePath());
    }

    // shortName, fullImportName
    Map<String, String> importsMap = map();
    // TODO - Use imports to expand variable types to their full type names, including modules

    private void attachImports(AbstractPythonScope scope) {
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
        FUNCTION_NAME, FUNCTION_PARAMS,
        DECORATOR_NAME, DECORATOR_PARAMS,
        POSSIBLE_VARIABLE, VARIABLE_TYPE,
        FROM_IMPORT, IMPORT_AS
    }
    private ParsePhase parsePhase = ParsePhase.START;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '(') numOpenParen++;
        if (type == ')') numOpenParen--;
        if (type == '{') numOpenBrace++;
        if (type == '}') numOpenBrace--;
        if (type == '[') numOpenBracket++;
        if (type == ']') numOpenBracket--;

        if (type == '\n') spaceDepth = 0;

        if (type == ' ') spaceDepth++;
        if (type == '\t') spaceDepth += 3;

        if ((type == '\'' || type == '"') && stringValue == null) {
            isInString = !isInString;
        }

        if (type == '@' && !isInMethod() && !isInString) {
            parsePhase = DECORATOR_NAME;
        }

        switch (parsePhase) {
            case START:            processStart           (type, lineNumber, stringValue); break;
            case CLASS_NAME:       processClassName       (type, lineNumber, stringValue); break;
            case CLASS_BASE_TYPES: processClassBaseTypes  (type, lineNumber, stringValue); break;
            case FUNCTION_NAME:    processFunctionName    (type, lineNumber, stringValue); break;
            case FUNCTION_PARAMS:  processFunctionParams  (type, lineNumber, stringValue); break;
            case DECORATOR_NAME:   processDecoratorName   (type, lineNumber, stringValue); break;
            case DECORATOR_PARAMS: processDecoratorParams (type, lineNumber, stringValue); break;
            case POSSIBLE_VARIABLE:processPossibleVariable(type, lineNumber, stringValue); break;
            case VARIABLE_TYPE:    processVariableType    (type, lineNumber, stringValue); break;
            case FROM_IMPORT:      processFromImport      (type, lineNumber, stringValue); break;
            case IMPORT_AS:        processImportAs        (type, lineNumber, stringValue); break;
        }

        if (stringValue != null) lastString = stringValue;
        if (type > 0) lastType = type;
    }

    private void processStart(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (stringValue.equals("class")) {
                parsePhase = ParsePhase.CLASS_NAME;
            } else if (stringValue.equals("def")) {
                parsePhase = ParsePhase.FUNCTION_NAME;
            } else if (!isInMethod() && !isInClass() && numOpenParen == 0 && !isInString) {
                if (stringValue.equals("from")) {
                    parsePhase = FROM_IMPORT;
                } else if (stringValue.equals("import")) {
                    parsePhase = IMPORT_AS;
                }
            }
        } else if (type == '=' && !isInMethod() && numOpenParen == 0 && !isInString) {
            parsePhase = POSSIBLE_VARIABLE;
        }
    }

    private void processClassName(int type, int lineNumber, String stringValue) {
        if (isInClass() || isInMethod()) {
            //  Not supporting embedded classes
            parsePhase = ParsePhase.START;
            return;
        }
        if (stringValue != null) {
            currentClass = new PythonClass();
            currentClass.setName(stringValue);
            currentClass.setSourceCodeLine(lineNumber);
            currentClass.setSourceCodePath(this.thisModule.getSourceCodePath());
            classEntrySpaceDepth = spaceDepth;

            for (PythonDecorator decorator : pendingDecorators) {
                currentClass.addDecorator(decorator);
            }
            pendingDecorators.clear();

            parsePhase = ParsePhase.CLASS_BASE_TYPES;
        }
    }

    private void processClassBaseTypes(int type, int lineNumber, String stringValue) {
        if (numOpenParen == 0) {
            attachImports(currentClass);
            registerScopeOutput(currentClass);
            parsePhase = ParsePhase.START;
        } else if (stringValue != null) {
            currentClass.addBaseType(stringValue);
        }
    }

    private void processFunctionName(int type, int lineNumber, String stringValue) {
        if (isInMethod()) {
            //  Not supporting embedded functions
            parsePhase = ParsePhase.START;
            return;
        }

        if (stringValue != null) {
            functionEntrySpaceDepth = spaceDepth;
            if (isInClass()) {
                currentFunction = new PythonFunction();
                currentFunction.setParentScope(currentClass);
            } else {
                currentFunction = new PythonFunction();
            }

            currentFunction.setSourceCodePath(this.thisModule.getSourceCodePath());

            for (PythonDecorator decorator : pendingDecorators) {
                currentFunction.addDecorator(decorator);
            }

            pendingDecorators.clear();

            currentFunction.setName(stringValue);
            currentFunction.setSourceCodeLine(lineNumber);

            parsePhase = ParsePhase.FUNCTION_PARAMS;
        }
    }

    private void processFunctionParams(int type, int lineNumber, String stringValue) {
        if (numOpenParen == 0) {
            if (currentFunction.getOwnerClass() == null) {
                //attachImports(currentFunction);
                registerScopeOutput(currentFunction);
            }
            parsePhase = ParsePhase.START;
        } else if (stringValue != null) {
            currentFunction.addParam(stringValue);
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

    PythonPublicVariable workingVariable;
    String workingVariableType = null;
    private void processPossibleVariable(int type, int lineNumber, String stringValue) {
        if (workingVariable == null) {
            workingVariable = new PythonPublicVariable();
            workingVariable.setSourceCodeLine(lineNumber);
            workingVariable.setName(lastString);
            workingVariable.setSourceCodePath(this.thisModule.getSourceCodePath());
        }

        if (stringValue != null) {
            workingVariableType = CodeParseUtil.buildTokenString(type, stringValue);
        } else {
            workingVariableType = "";
        }

        parsePhase = VARIABLE_TYPE;
    }

    private void processVariableType(int type, int lineNumber, String stringValue) {
        if (type == '(' || type == '\n') {
            workingVariable.setValueString(workingVariableType);
            String fullVarName = workingVariable.getName();
            if (currentClass != null) {
                fullVarName = currentClass.getFullName() + "." + fullVarName;
            }

            if (!hasParsedVariable(fullVarName)) {
                //attachImports(workingVariable);
                if (currentClass != null) {
                    currentClass.addChildScope(workingVariable);
                } else {
                    registerScopeOutput(workingVariable);
                }
            }
            workingVariable = null;
            workingVariableType = null;
            parsePhase = START;
        } else {
            workingVariableType += CodeParseUtil.buildTokenString(type, stringValue);
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
