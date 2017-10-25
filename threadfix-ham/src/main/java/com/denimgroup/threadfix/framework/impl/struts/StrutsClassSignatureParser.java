package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.*;

public class StrutsClassSignatureParser implements EventBasedTokenizer {

    String parsedClassName;
    List<StrutsMethod> methods = new ArrayList<StrutsMethod>();
    List<String> baseTypes = new ArrayList<String>();
    boolean skipBuiltIn = true;
    boolean skipNonPublic = true;
    boolean skipConstructors = true;
    boolean skipAccessors = true;



    public void setSkipBuiltIn(boolean shouldSkipBuiltInMethods) {
        skipBuiltIn = shouldSkipBuiltInMethods;
    }

    public void setSkipNonPublic(boolean shouldSkipNonPublicMethods) {
        skipNonPublic = shouldSkipNonPublicMethods;
    }

    public void setSkipConstructors(boolean shouldSkipConstructors) {
        skipConstructors = shouldSkipConstructors;
    }

    public void setSkipAccessors(boolean shouldSkipGetSetMethods) {
        skipAccessors = shouldSkipGetSetMethods;
    }



    public String getParsedClassName() {
        return parsedClassName;
    }

    public Collection<StrutsMethod> getParsedMethods() {
        return methods;
    }

    public Collection<String> getBaseTypes() { return baseTypes; }


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '{') numOpenBraces++;
        if (type == '}') numOpenBraces--;
        if (type == '(') numOpenParens++;
        if (type == ')') numOpenParens--;

        switch (parsePhase) {
            case IDENTIFICATION:
                processIdentificationPhase(type, lineNumber, stringValue);
                break;

            case PARSE_PACKAGE:
                processParsePackagePhase(type, lineNumber, stringValue);
                break;

            case CLASS_BASE_TYPES:
                processClassBaseTypesPhase(type, lineNumber, stringValue);
                break;

            case NEXT_IS_CLASS_NAME:
                parsedClassName = stringValue;
                parsePhase = ParsePhase.IDENTIFICATION;
                break;

            case IN_CLASS:
                processInClassPhase(type, lineNumber, stringValue);
                break;
        }

        if (type > 0)
            lastToken = type;
        if (stringValue != null)
            lastString = stringValue;
    }


    String lastString = null;
    int lastToken = -1;
    int numOpenBraces = 0;
    int numOpenParens = 0;


    enum ParsePhase { IDENTIFICATION, PARSE_PACKAGE, NEXT_IS_CLASS_NAME, CLASS_BASE_TYPES, IN_CLASS }
    ParsePhase parsePhase = ParsePhase.IDENTIFICATION;


    void processIdentificationPhase(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (stringValue.equals("class") && parsedClassName == null) {
                parsePhase = ParsePhase.NEXT_IS_CLASS_NAME;
            } else if (stringValue.equals("package")) {
                parsePhase = ParsePhase.PARSE_PACKAGE;
                workingPackageName = "";
            } else if (stringValue.equals("implements")) {
                parseBaseTypesState = ParseBaseTypesState.INTERFACES;
                parsePhase = ParsePhase.CLASS_BASE_TYPES;
                workingBaseTypeName = "";
            } else if (stringValue.equals("extends")) {
                parseBaseTypesState = ParseBaseTypesState.CLASSES;
                parsePhase = ParsePhase.CLASS_BASE_TYPES;
                workingBaseTypeName = "";
            }
        } else {
            if (type == '{') {
                parsePhase = ParsePhase.IN_CLASS;
            }
        }
    }



    String workingPackageName;
    void processParsePackagePhase(int type, int lineNumber, String stringValue) {
        if (type == ';') {
            parsePhase = ParsePhase.IDENTIFICATION;
        } else {
            workingPackageName += CodeParseUtil.buildTokenString(type, stringValue);
        }
    }


    enum ParseBaseTypesState { START, CLASSES, INTERFACES }
    ParseBaseTypesState parseBaseTypesState = ParseBaseTypesState.START;

    String workingBaseTypeName;
    void processClassBaseTypesPhase(int type, int lineNumber, String stringValue) {
        boolean endOfSection = false;

        if (type == '{') {
            endOfSection = true;
            parsePhase = ParsePhase.IN_CLASS;
        } else {

            if (stringValue != null) {
                if (stringValue.equals("implements")) {
                    parseBaseTypesState = ParseBaseTypesState.INTERFACES;
                    endOfSection = true;
                } else if (stringValue.equals("extends")) {
                    parseBaseTypesState = ParseBaseTypesState.CLASSES;
                    endOfSection = true;
                }
            }

            if (stringValue == null || (!stringValue.equals("implements") && !stringValue.equals("extends"))) {
                switch (parseBaseTypesState) {
                    case START:
                        break;

                    case CLASSES:
                        workingBaseTypeName += CodeParseUtil.buildTokenString(type, stringValue);
                        break;

                    case INTERFACES:
                        workingBaseTypeName += CodeParseUtil.buildTokenString(type, stringValue);
                        break;
                }
            }
        }

        if (endOfSection) {
            if (workingBaseTypeName != null && workingBaseTypeName.length() > 0) {
                String[] parsedTypes = CodeParseUtil.splitByComma(workingBaseTypeName);
                baseTypes.addAll(Arrays.asList(parsedTypes));
            }

            workingBaseTypeName = "";
        }
    }



    boolean isBuiltInMethod(String methodName) {
        return methodName.equals("toString") ||
                methodName.equals("hashCode") ||
                methodName.equals("equals") ||
                methodName.equals("finalize") ||
                methodName.equals("clone");
    }




    String possibleMethodName;
    String possibleMethodParams;
    boolean isPublicMethod;

    enum InClassState { IDENTIFICATION, POSSIBLE_METHOD_PARAMS_START, POSSIBLE_METHOD_PARAMS_END }
    InClassState inClassState = InClassState.IDENTIFICATION;

    void processInClassPhase(int type, int lineNumber, String stringValue) {

        switch (inClassState) {
            case IDENTIFICATION:
                //  If we're in some function definition
                if (numOpenBraces > 1) {
                    return;
                }

                if (numOpenBraces == 0) {
                    parsePhase = ParsePhase.IDENTIFICATION;
                    return;
                }

                if (possibleMethodName != null && type == '(' && lastToken != '@') {
                    inClassState = InClassState.POSSIBLE_METHOD_PARAMS_START;
                } else if (stringValue != null) {
                    if (stringValue.equals("private")) {
                        isPublicMethod = false;
                    } else if (stringValue.equals("protected")) {
                        isPublicMethod = false;
                    } else if (stringValue.equals("public")) {
                        isPublicMethod = true;
                    } else {
                        possibleMethodName = stringValue;
                    }
                }
                break;

            case POSSIBLE_METHOD_PARAMS_START:
                if (type == ')' && numOpenParens == 0) {
                    inClassState = InClassState.POSSIBLE_METHOD_PARAMS_END;
                    break;
                }

                possibleMethodParams += CodeParseUtil.buildTokenString(type, stringValue);

                break;

            case POSSIBLE_METHOD_PARAMS_END:

                boolean canParse = true;
                if (skipNonPublic && !isPublicMethod) {
                    canParse = false;
                }

                if (skipConstructors && possibleMethodName.equals(parsedClassName)) {
                    canParse = false;
                }

                if (skipBuiltIn && isBuiltInMethod(possibleMethodName)) {
                    canParse = false;
                }

                if (skipAccessors && (possibleMethodName.startsWith("get") || possibleMethodName.startsWith("set"))) {
                    canParse = false;
                }

                if (type == '{' && canParse) {

                    StrutsMethod newMethod = new StrutsMethod();
                    newMethod.setName(possibleMethodName);

                    String[] splitParams = CodeParseUtil.splitByComma(possibleMethodParams);
                    for (String param : splitParams) {
                        String[] paramParts = param.split(" ");
                        String paramName = paramParts[paramParts.length - 1];

                        newMethod.addParameter(paramName);
                    }
                    methods.add(newMethod);
                }

                possibleMethodName = "";
                possibleMethodParams = "";
                isPublicMethod = false;
                inClassState = InClassState.IDENTIFICATION;
                parsePhase = ParsePhase.IN_CLASS;
                break;
        }

    }
}
