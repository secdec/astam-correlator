package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonSyntaxParser implements EventBasedTokenizer {

    public static PythonCodeCollection run(File rootDirectory) {
        PythonCodeCollection codebase = new PythonCodeCollection();
        Collection<File> allFiles = FileUtils.listFiles(rootDirectory, new String[] { "py" }, true);
        for (File file : allFiles) {

            if (FilePathUtils.getRelativePath(file, rootDirectory).contains("build")) {
                continue;
            }

            PythonSyntaxParser parser = new PythonSyntaxParser();
            EventBasedTokenizerRunner.run(file, DjangoTokenizerConfigurator.INSTANCE, parser);

            Collection<PythonClass> classes = parser.getClasses();
            Collection<PythonFunction> globalFunctions = parser.getGlobalFunctions();

            if (classes.size() > 0) {
                codebase.addClasses(file.getAbsolutePath(), classes);
            }

            if (globalFunctions.size() > 0) {
                codebase.addFunctions(file.getAbsolutePath(), globalFunctions);
            }
        }
        return codebase;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    String currentName = null;
    String currentParams = null;
    PythonClass currentClass = null;
    PythonFunction currentFunction = null;

    String lastString;
    int lastType;

    int numOpenParen = 0;
    int numOpenBrace = 0;
    int numOpenBracket = 0;

    int spaceDepth = 0;
    int classEntrySpaceDepth = -1;
    int functionEntrySpaceDepth = -1;

    List<PythonClass> classes = list();
    List<PythonFunction> globalFunctions = list();

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

    enum ParsePhase { START, CLASS_NAME, CLASS_BASE_TYPES, FUNCTION_NAME, FUNCTION_PARAMS }
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

        switch (parsePhase) {
            case START:            processStart          (type, lineNumber, stringValue); break;
            case CLASS_NAME:       processClassName      (type, lineNumber, stringValue); break;
            case CLASS_BASE_TYPES: processClassBaseTypes (type, lineNumber, stringValue); break;
            case FUNCTION_NAME:    processFunctionName   (type, lineNumber, stringValue); break;
            case FUNCTION_PARAMS:  processFunctionParams (type, lineNumber, stringValue); break;
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
            currentClass.setLineNumber(lineNumber);
            classEntrySpaceDepth = spaceDepth;
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
                currentFunction = new PythonFunction(currentClass);
                currentClass.addFunction(currentFunction);
            } else {
                currentFunction = new PythonFunction(null);
            }

            currentFunction.setMethodName(stringValue);
            currentFunction.setLineNumber(lineNumber);

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
}
