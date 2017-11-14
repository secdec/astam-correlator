package com.denimgroup.threadfix.framework.impl.django.python;

import com.denimgroup.threadfix.framework.impl.django.DjangoTokenizerConfigurator;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.DECORATOR_NAME;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.DECORATOR_PARAMS;
import static com.denimgroup.threadfix.framework.impl.django.python.PythonSyntaxParser.ParsePhase.START;

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
                PythonSyntaxParser parser = new PythonSyntaxParser();
                EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);

                Collection<PythonClass> classes = parser.getClasses();
                Collection<PythonFunction> globalFunctions = parser.getGlobalFunctions();

                for (PythonClass pyClass : classes) {
                    pyClass.setSourceCodePath(file.getAbsolutePath());
                }

                for (PythonFunction pyFunction : globalFunctions) {
                    pyFunction.setSourceCodePath(file.getAbsolutePath());
                }

                result.addAll(classes);
                result.addAll(globalFunctions);
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

    enum ParsePhase { START, CLASS_NAME, CLASS_BASE_TYPES, FUNCTION_NAME, FUNCTION_PARAMS, DECORATOR_NAME, DECORATOR_PARAMS }
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
            case START:            processStart          (type, lineNumber, stringValue); break;
            case CLASS_NAME:       processClassName      (type, lineNumber, stringValue); break;
            case CLASS_BASE_TYPES: processClassBaseTypes (type, lineNumber, stringValue); break;
            case FUNCTION_NAME:    processFunctionName   (type, lineNumber, stringValue); break;
            case FUNCTION_PARAMS:  processFunctionParams (type, lineNumber, stringValue); break;
            case DECORATOR_NAME:   processDecoratorName  (type, lineNumber, stringValue); break;
            case DECORATOR_PARAMS: processDecoratorParams(type, lineNumber, stringValue); break;
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
            }
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
