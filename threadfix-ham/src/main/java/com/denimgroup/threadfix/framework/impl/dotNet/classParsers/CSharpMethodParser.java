////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.isValidTypeName;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.tokenIsValidInTypeName;

public class CSharpMethodParser extends AbstractCSharpParser<CSharpMethod> implements EventBasedTokenizer {

    //  NOTE - This does not catch constructors

    CSharpParameterParser parameterParser;
    CSharpAttributeParser attributeParser;
    CSharpScopeTracker scopeTracker;

    private int classBraceLevel = -1;

    @Override
    public void setParsingContext(CSharpParsingContext context) {
        parameterParser = context.getParameterParser();
        attributeParser = context.getAttributeParser();
        scopeTracker = context.getScopeTracker();
    }

    public void setClassBraceLevel(int braceLevel) {
        this.classBraceLevel = braceLevel;
    }

    @Override
    public void reset() {
        super.reset();
        classBraceLevel = -1;
        currentMethodState = MethodState.SEARCH;
        clearPossibleMethodData();
    }



    private enum MethodState {
        SEARCH,
        PARAMETERS,
        METHOD_BODY,
        MAYBE_ARROW_METHOD,
        ARROW_METHOD
    }

    private MethodState currentMethodState = MethodState.SEARCH;
    private CSharpMethod.AccessLevel possibleAccessLevel = CSharpMethod.AccessLevel.PRIVATE;
    private String possibleMethodReturnType = null;
    private boolean isPossibleStaticMethod = false;
    private String workingString = null;
    private List<CSharpAttribute> pendingAttributes = list();

    private void clearPossibleMethodData() {
        possibleAccessLevel = CSharpMethod.AccessLevel.PRIVATE;
        possibleMethodReturnType = null;
        isPossibleStaticMethod = false;
        workingString = null;
        pendingAttributes.clear();
    }

    private static boolean isNullOrWhitespace(String string) {
        return string == null || string.trim().isEmpty();
    }



    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        if (isDisabled() || scopeTracker.isInComment()) {
            return;
        }

        switch (currentMethodState) {
            case SEARCH:
                boolean madeAttributeItem = false;
                if (attributeParser.isBuildingItem()) {
                    break;
                } else while (attributeParser.hasItem()) {
                    pendingAttributes.add(attributeParser.pullCurrentItem());
                    madeAttributeItem = true;
                }

                if (madeAttributeItem) {
                    break;
                }

                //  Occurs during a property definition
                if (scopeTracker.getNumOpenBrace() > classBraceLevel) {
                    clearPossibleMethodData();
                    break;
                }

                if (stringValue != null) {
                    if (stringValue.equals(PUBLIC)) {
                        possibleAccessLevel = CSharpMethod.AccessLevel.PUBLIC;
                        workingString = null;
                    } else if (stringValue.equals(PROTECTED)) {
                        possibleAccessLevel = CSharpMethod.AccessLevel.PROTECTED;
                        workingString = null;
                    } else if (stringValue.equals(PRIVATE)) {
                        possibleAccessLevel = CSharpMethod.AccessLevel.PRIVATE;
                        workingString = null;
                    } else if (stringValue.equals(STATIC)) {
                        isPossibleStaticMethod = true;
                        workingString = null;
                    } else if (stringValue.equals(ABSTRACT) || stringValue.equals(OPERATOR)) {
                        //  Ignore these methods
                        clearPossibleMethodData();
                        break;
                    } else if (CS_KEYWORDS.contains(stringValue)) {
                        //  Some reserved keyword we don't care about (ie async, override, virtual)
                        break;
                    } else if (type < 0) {
                        if (workingString == null) {
                            workingString = stringValue;
                        } else if (isValidTypeName(workingString)) {
                            possibleMethodReturnType = workingString;
                            workingString = stringValue;
                        } else {
                            workingString += CodeParseUtil.buildTokenString(type, stringValue);
                        }
                    } else {
                        clearPossibleMethodData();
                    }
                } else if (type == '(') {
                    String possibleMethodName = workingString;

                    if (isNullOrWhitespace(possibleMethodReturnType) || isNullOrWhitespace(possibleMethodName)) {
                        clearPossibleMethodData();
                        break;
                    }

                    CSharpMethod pendingMethod = new CSharpMethod();
                    pendingMethod.setName(possibleMethodName);
                    pendingMethod.setReturnType(possibleMethodReturnType);
                    pendingMethod.setIsStatic(isPossibleStaticMethod);
                    pendingMethod.setAccessLevel(possibleAccessLevel);
                    pendingMethod.setStartLine(lineNumber);

                    for (CSharpAttribute attribute : pendingAttributes) {
                        pendingMethod.addAttribute(attribute);
                    }

                    setPendingItem(pendingMethod);

                    clearPossibleMethodData();
                    //  Clear old detected parameters
                    parameterParser.clearItems();
                    currentMethodState = MethodState.PARAMETERS;
                } else if (type == ';' || type == '=' || type == ')') {
                    clearPossibleMethodData();
                } else if (tokenIsValidInTypeName((char)type)) {
                    workingString += (char)type;
                }
                break;

            case PARAMETERS:
                while (parameterParser.hasItem()) {
                    getPendingItem().addParameter(parameterParser.pullCurrentItem());
                }

                if (parameterParser.isBuildingItem()) {
                    break;
                }

                if (scopeTracker.getNumOpenParen() == 0) {
                    currentMethodState = MethodState.METHOD_BODY;
                    attributeParser.disable();
                    parameterParser.disable();
                } else if (type == ';') {
                    //  Abstract method
                    setPendingItem(null);
                    clearPossibleMethodData();
                    currentMethodState = MethodState.SEARCH;
                }
                break;

            case METHOD_BODY:
                if (type == '=' && scopeTracker.getNumOpenBrace() <= classBraceLevel) {
                    currentMethodState = MethodState.MAYBE_ARROW_METHOD;
                } else if (type == '}' && scopeTracker.getNumOpenBrace() <= classBraceLevel) {
                    getPendingItem().setEndLine(lineNumber);
                    finalizePendingItem();
                    attributeParser.reset();
                    parameterParser.reset();
                    attributeParser.enable();
                    parameterParser.enable();
                    currentMethodState = MethodState.SEARCH;
                }
                break;

            case MAYBE_ARROW_METHOD:
                if (type == '>') {
                    currentMethodState = MethodState.ARROW_METHOD;
                    attributeParser.disable();
                    parameterParser.disable();
                } else {
                    setPendingItem(null);
                    clearPossibleMethodData();
                    currentMethodState = MethodState.SEARCH;
                }
                break;

            case ARROW_METHOD:
                if (type == ';' && scopeTracker.getNumOpenParen() == 0 && scopeTracker.getNumOpenBrace() <= classBraceLevel) {
                    getPendingItem().setEndLine(lineNumber);
                    finalizePendingItem();
                    attributeParser.reset();
                    parameterParser.reset();
                    attributeParser.enable();
                    parameterParser.enable();
                    currentMethodState = MethodState.SEARCH;
                }
                break;
        }
    }
}
