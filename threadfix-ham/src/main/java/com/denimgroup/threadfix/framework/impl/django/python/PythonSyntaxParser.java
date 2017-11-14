package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.DjangoTokenizerConfigurator;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.*;

public class PythonSyntaxParser implements EventBasedTokenizer {

    public static PythonCodeCollection run(File rootDirectory) {
        PythonCodeCollection codebase = new PythonCodeCollection();
        if (rootDirectory.isFile()) {
            PythonSyntaxParser parser = new PythonSyntaxParser();
            EventBasedTokenizerRunner.run(rootDirectory, DjangoTokenizerConfigurator.INSTANCE, parser);
            for (PythonFunction pyFunction : parser.getGlobalFunctions()) {
                pyFunction.setSourceCodePath(rootDirectory.getAbsolutePath());
                codebase.add(pyFunction);
            }
            for (PythonClass pyClass : parser.getClasses()) {
                pyClass.setSourceCodePath(rootDirectory.getAbsolutePath());
                codebase.add(pyClass);
            }
        } else {
            for (AbstractPythonScope scope : recurseCodeDirectory(rootDirectory)) {
                codebase.add(scope);
            }
        }
        return codebase;
    }

    private static Collection<AbstractPythonScope> recurseCodeDirectory(File rootDirectory) {
        List<AbstractPythonScope> result = new LinkedList<AbstractPythonScope>();
        File[] allFiles = rootDirectory.listFiles();
        for (File file : allFiles) {
            if (file.isFile()) {
                if (!file.getName().endsWith(".py")) {
                    continue;
                }

                PythonSyntaxParser parser = new PythonSyntaxParser();
                EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);

                Collection<PythonClass> classes = parser.getClasses();
                Collection<PythonFunction> globalFunctions = parser.getGlobalFunctions();
                Collection<PythonPublicVariable> publicVariables = parser.getPublicVariables();

                for (PythonClass pyClass : classes) {
                    pyClass.setSourceCodePath(file.getAbsolutePath());
                }

                for (PythonFunction pyFunction : globalFunctions) {
                    pyFunction.setSourceCodePath(file.getAbsolutePath());
                }

                for (PythonPublicVariable pyVariable : publicVariables) {
                    pyVariable.setSourceCodePath(file.getAbsolutePath());
                }

                result.addAll(classes);
                result.addAll(globalFunctions);
                result.addAll(publicVariables);
            } else {
                PythonModule module = new PythonModule();
                module.setSourceCodePath(file.getAbsolutePath());
                module.setName(file.getName());
                for (AbstractPythonScope scope : recurseCodeDirectory(file)) {
                    module.addChildScope(scope);
                }
                result.add(module);
            }
        }
        return result;
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

    List<PythonClass> classes = list();
    List<PythonFunction> globalFunctions = list();
    List<PythonDecorator> pendingDecorators = list();
    List<PythonPublicVariable> publicVariables = list();

    // shortName, fullImportName
    Map<String, String> importsMap = map();
    // TODO - Use imports to expand variable types to their full type names, including modules

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
            classes.add(currentClass);
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
                currentFunction.setParentModule(currentClass);
            } else {
                currentFunction = new PythonFunction();
            }

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
                globalFunctions.add(currentFunction);
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
            workingVariable.setTypeName(workingVariableType);
            String fullVarName = workingVariable.getName();
            if (currentClass != null) {
                fullVarName = currentClass.getFullName() + "." + fullVarName;
            }

            if (!hasParsedVariable(fullVarName)) {
                if (currentClass != null) {
                    currentClass.addChildScope(workingVariable);
                } else {
                    publicVariables.add(workingVariable);
                }
            }
            workingVariable = null;
            workingVariableType = null;
            parsePhase = START;
        } else {
            workingVariableType += CodeParseUtil.buildTokenString(type, stringValue);
        }
    }

    String importName, importItem;

    private void processFromImport(int type, int lineNumber, String stringValue) {
        //
        if (type == '\n' && (importName == null || importItem == null)) {
            importName = null;
            importItem = null;
            parsePhase = START;
        }

        if (stringValue != null && !stringValue.equals("import")) {
            if (importName == null) {
                importName = stringValue;
            } else {
                importItem = stringValue;
                parsePhase = START;

                importsMap.put(importItem, importName);
                importName = null;
                importItem = null;
            }
        }
    }

    private void processImportAs(int type, int lineNumber, String stringValue) {
        if (stringValue != null && !stringValue.equals("as")) {
            if (importName == null) {
                importName = stringValue;
            } else {
                importItem = stringValue;
                parsePhase = START;

                importsMap.put(importItem, importName);
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
