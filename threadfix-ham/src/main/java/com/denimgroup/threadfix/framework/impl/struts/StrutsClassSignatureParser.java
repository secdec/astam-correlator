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

package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsMethod;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

public class StrutsClassSignatureParser implements EventBasedTokenizer {

    String parsedClassName;
    List<StrutsMethod> methods = new ArrayList<StrutsMethod>();
    List<String> baseTypes = new ArrayList<String>();
    List<String> imports = new ArrayList<String>();
    Set<ModelField> parameters = new HashSet<ModelField>();
    String classPackage;
    boolean skipBuiltIn = true;
    boolean skipNonPublic = true;
    boolean skipConstructors = true;
    boolean isInterface = false;
    ScopeTracker scopeTracker = new ScopeTracker();



    public void setSkipBuiltIn(boolean shouldSkipBuiltInMethods) {
        skipBuiltIn = shouldSkipBuiltInMethods;
    }

    public void setSkipNonPublic(boolean shouldSkipNonPublicMethods) {
        skipNonPublic = shouldSkipNonPublicMethods;
    }

    public void setSkipConstructors(boolean shouldSkipConstructors) {
        skipConstructors = shouldSkipConstructors;
    }



    public String getParsedClassName() {
        return parsedClassName;
    }

    public Collection<StrutsMethod> getParsedMethods() {
        return methods;
    }

    public Set<ModelField> getParameters() { return parameters; };

    public Collection<String> getBaseTypes() { return baseTypes; }

    public String getClassPackage() { return classPackage;}

    public Collection<String> getImports() { return imports; }

    public Set<ModelField> getFields() { return fields; }


    @Override
    public boolean shouldContinue() {
        return !isInterface;
    }

    int lineno = -1;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (lineNumber != lineno) {
            lineno = lineNumber;
        }

        // Parsing can break (and is not necessary) for interfaces
        if (isInterface) {
            return;
        }

        if (type == '{') numOpenBraces++;
        if (type == '}') numOpenBraces--;
        if (type == '(') numOpenParens++;
        if (type == ')') numOpenParens--;

        scopeTracker.interpretToken(type);
        if (stringValue != null) {
            scopeTracker.interpretToken(type);
        }

        switch (parsePhase) {
            case IDENTIFICATION:
                processIdentificationPhase(type, lineNumber, stringValue);
                break;

            case PARSE_PACKAGE:
                processParsePackagePhase(type, lineNumber, stringValue);
                break;

            case PARSE_IMPORTS:
                processParseImportsPhase(type, lineNumber, stringValue);
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


    enum ParsePhase { IDENTIFICATION, PARSE_PACKAGE, PARSE_IMPORTS, NEXT_IS_CLASS_NAME, CLASS_BASE_TYPES, IN_CLASS }
    ParsePhase parsePhase = ParsePhase.IDENTIFICATION;


    void processIdentificationPhase(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (stringValue.equals("class") && parsedClassName == null) {
                parsePhase = ParsePhase.NEXT_IS_CLASS_NAME;
            } else if (stringValue.equals("interface")) {
                isInterface = true;
            } else if (stringValue.equals("package")) {
                parsePhase = ParsePhase.PARSE_PACKAGE;
                workingPackageName = "";
            } else if (stringValue.equals("import")) {
                parsePhase = ParsePhase.PARSE_IMPORTS;
                workingImportName = "";
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
            if (type == '{' && parsedClassName != null) {
                parsePhase = ParsePhase.IN_CLASS;
            }
        }
    }



    String workingPackageName;
    void processParsePackagePhase(int type, int lineNumber, String stringValue) {
        if (type == ';') {
            classPackage = workingPackageName;
            parsePhase = ParsePhase.IDENTIFICATION;
        } else {
            workingPackageName += CodeParseUtil.buildTokenString(type, stringValue);
        }
    }


    String workingImportName;
    void processParseImportsPhase(int type, int lineNumber, String stringValue) {
        if (type == ';') {
            imports.add(workingImportName);
            workingImportName = null;
            parsePhase = ParsePhase.IDENTIFICATION;
        } else {
            workingImportName += CodeParseUtil.buildTokenString(type, stringValue);
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




    String possibleMethodReturnValue;
    String possibleMethodName;
    String possibleMethodParams;
    boolean isPublicMember;
    boolean isArray;
    int methodStartBraceLevel = -1;
    int methodStartLine = -1;
    Set<ModelField> fields = set();

    enum InClassState { IDENTIFICATION, POSSIBLE_METHOD_PARAMS_START, POSSIBLE_METHOD_PARAMS_END, IN_METHOD }
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

                if (possibleMethodName != null && possibleMethodReturnValue != null && type == '(') {
                    methodStartLine = lineNumber;
                    inClassState = InClassState.POSSIBLE_METHOD_PARAMS_START;
                    isArray = isArray || lastToken == ']';
                } else if (stringValue != null) {
                    if (stringValue.equals("private")) {
                        isPublicMember = false;
                    } else if (stringValue.equals("protected")) {
                        isPublicMember = false;
                    } else if (stringValue.equals("public")) {
                        isPublicMember = true;
                    } else {
                        isArray = isCollectionType(lastString) || (possibleMethodReturnValue != null && isCollectionType(possibleMethodReturnValue));
                        possibleMethodReturnValue = possibleMethodName;
                        possibleMethodName = stringValue;
                    }
                } else if ((type == ';' || type == '=') && possibleMethodReturnValue != null && possibleMethodName != null) {
                    ModelField newField = new ModelField(possibleMethodReturnValue, possibleMethodName);
                    fields.add(newField);
                    possibleMethodName = null;
                    possibleMethodReturnValue = null;
                }
                break;

            case POSSIBLE_METHOD_PARAMS_START:
                if (type == ')' && numOpenParens == 0) {
                    inClassState = InClassState.POSSIBLE_METHOD_PARAMS_END;
                    break;
                }

                if (possibleMethodParams == null) {
                    possibleMethodParams = "";
                }

                if (!possibleMethodParams.isEmpty()) {
                    possibleMethodParams += ' ';
                }
                possibleMethodParams += CodeParseUtil.buildTokenString(type, stringValue);

                break;

            case POSSIBLE_METHOD_PARAMS_END:

                boolean canParse = true;
                if (skipNonPublic && !isPublicMember) {
                    canParse = false;
                }

                if (skipConstructors && possibleMethodName.equals(parsedClassName)) {
                    canParse = false;
                }

                if (skipBuiltIn && isBuiltInMethod(possibleMethodName)) {
                    canParse = false;
                }

                if ((type == '{' || (stringValue != null && stringValue.equals("throws"))) && canParse) {

                    StrutsMethod newMethod = new StrutsMethod();
                    String methodName = possibleMethodName;
                    newMethod.setName(methodName);
                    newMethod.setReturnType(possibleMethodReturnValue + (isArray ? "[]" : ""));
                    newMethod.setStartLine(methodStartLine);

                    if (possibleMethodParams == null) {
                        possibleMethodParams = "";
                    }

                    String[] splitParams = CodeParseUtil.splitByComma(possibleMethodParams);
                    for (String param : splitParams) {
                        boolean paramIsArray = isCollectionType(param);
                        String[] paramParts = param.split(" ");
                        String paramName = null, paramType = null;
                        for (String part : paramParts) {
                            if (!isCollectionType(part) && isValidIdentifier(part)) {
                                paramType = paramName;
                                paramName = part;
                            }
                        }
                        newMethod.addParameter(paramName, paramType + (paramIsArray ? "[]" : ""));
                    }
                    methods.add(newMethod);

                    methodStartBraceLevel = scopeTracker.getNumOpenBrace();
                    if (stringValue != null && stringValue.equals("throws")) {
                        //  We've marked this as a method but aren't actually in the method yet
                        //  Brace count will be off-by-one, correct this
                        methodStartBraceLevel += 1;
                    }
                    inClassState = InClassState.IN_METHOD;
                } else if (type == ';') {
                    inClassState = InClassState.IDENTIFICATION;
                }

                possibleMethodName = "";
                possibleMethodParams = "";
                isPublicMember = false;
                isArray = false;
                methodStartLine = -1;
                break;

            case IN_METHOD:
                StrutsMethod currentMethod = methods.get(methods.size() - 1);
                if (scopeTracker.getNumOpenBrace() < methodStartBraceLevel) {
                    methodStartBraceLevel = -1;
                    currentMethod.setEndLine(lineNumber);
                    inClassState = InClassState.IDENTIFICATION;
                } else {
                    if (stringValue != null) {
                        currentMethod.addSymbolReference(stringValue);
                    }
                }
                break;
        }

    }

    boolean isCollectionType(String typeName) {
        return typeName.contains("List") || typeName.contains("Collection");
    }

    boolean isValidIdentifier(String code) {
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c < 48) {
                return false;
            } else if (c > 57 && c < 65) {
                return false;
            } else if (c > 90 && c < 97 && c != 95) {
                return false;
            } else if (c > 122) {
                return false;
            }
        }
        return true;
    }
}
