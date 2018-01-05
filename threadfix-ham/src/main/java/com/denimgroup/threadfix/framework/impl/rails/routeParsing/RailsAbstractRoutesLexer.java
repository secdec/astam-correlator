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

package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.io.StreamTokenizer;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  NOTE: This parser is sensitive to newlines since the Rails routing syntax
//      is also sensitive to newlines
//  Conditional statements are IGNORED due to the complexity of their variations
public class RailsAbstractRoutesLexer implements EventBasedTokenizer {

    static final SanitizedLogger LOG = new SanitizedLogger(RailsAbstractRoutesLexer.class.getName());


    RailsAbstractRoutingTree resultTree = new RailsAbstractRoutingTree();
    RailsAbstractRouteEntryDescriptor currentDescriptor = null;
    List<RailsAbstractRouteEntryDescriptor> scopeStack = list();

    public RailsAbstractRoutingTree getResultTree() {
        return resultTree;
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    String workingLine = "";
    int numOpenParen = 0;
    int numOpenBrace = 0;
    int numOpenBracket = 0;

    String lastString;
    int lastType;
    boolean wasRouteScope = false;
    boolean isInComment = false;
    boolean isInString = false;
    boolean wasHashParameter = false;

    // Syntax is generally newline-sensitive except in the case of parameters, keep track of
    //  whether or not we're still parsing an entry even if a newline is reached.
    boolean spansNextLine = false;
    // Notes:
    //   Parameters continuing on the next line must end in a comma
    //          OK: get 'endpoint' , \n action: 'something'
    //      Not OK: get 'endpoint' \n , action: 'something'

    //  Whenever conditionalDepth > 0 we ignore route parsing
    int conditionalDepth = 0;


    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (type == '{') numOpenBrace++;
        if (type == '}') numOpenBrace--;
        if (type == '(') numOpenParen++;
        if (type == ')') numOpenParen--;
        if (type == '[') numOpenBracket++;
        if (type == ']') numOpenBracket--;

        if (type == '#') {
            isInComment = true;
        }

        if (isInComment) {
            if (type == StreamTokenizer.TT_EOL) {
                isInComment = false;
            }
        }

        if (stringValue != null) {
            if (stringValue.equalsIgnoreCase("if") && parsePhase == ParsePhase.SEARCH_IDENTIFIER) {
                conditionalDepth++;
                return;
            } else if (conditionalDepth > 0) {
                if (stringValue.equalsIgnoreCase("do")) {
                    conditionalDepth++;
                } else if (stringValue.equalsIgnoreCase("end")) {
                    conditionalDepth--;
                }
                return;
            }
        }

        if (!isInComment && conditionalDepth == 0) {
            if ((type == '\'' || type == '"') && stringValue == null) {
                isInString = !isInString;
            }

            switch (parsePhase) {

                case START_RENDER:
                    processStartRenderPhase(type, lineNumber, stringValue);
                    break;

                case SEARCH_IDENTIFIER:
                    processSearchIdentifierPhase(type, lineNumber, stringValue);
                    break;

                case PARAMETERS:
                    processParametersPhase(type, lineNumber, stringValue);
                    break;

                case INITIALIZER_PARAMETERS:
                    processInitializerParametersPhase(type, lineNumber, stringValue);
                    break;
            }
        }


        if (stringValue != null) {
            lastString = stringValue;
        }

        lastType = type;
    }

    RailsAbstractRouteEntryDescriptor getCurrentScope() {
        if (scopeStack.size() == 0) {
            return null;
        } else {
            return scopeStack.get(scopeStack.size() - 1);
        }
    }

    boolean isEntryScope() {
        return numOpenBrace == 0 && numOpenParen == 0 && numOpenBracket == 0;
    }

    enum ParsePhase { START_RENDER, SEARCH_IDENTIFIER, PARAMETERS, INITIALIZER_PARAMETERS }
    ParsePhase parsePhase = ParsePhase.START_RENDER;

    void processStartRenderPhase(int type, int lineNumber, String stringValue) {
        if (stringValue != null && stringValue.equalsIgnoreCase("do")) {
            parsePhase = ParsePhase.SEARCH_IDENTIFIER;
            RailsAbstractRouteEntryDescriptor rootDescriptor = new RailsAbstractRouteEntryDescriptor();
            rootDescriptor.setIdentifier(lastString);
            scopeStack.add(rootDescriptor);
            resultTree.setRootDescriptor(rootDescriptor);
        }
    }

    void processSearchIdentifierPhase(int type, int lineNumber, String stringValue) {
        if (stringValue != null) {
            if (stringValue.equalsIgnoreCase("end")) {
                if (scopeStack.size() > 0) {
                    scopeStack.remove(scopeStack.size() - 1);
                }
            } else {
                RailsAbstractRouteEntryDescriptor descriptor = new RailsAbstractRouteEntryDescriptor();
                descriptor.setIdentifier(stringValue);
                descriptor.setParentDescriptor(getCurrentScope());
                descriptor.setLineNumber(lineNumber);
                currentDescriptor = descriptor;
                parsePhase = ParsePhase.PARAMETERS;
            }
        }
    }

    RouteParameterValueType detectParameterValueType(String paramValue) {
        if (paramValue.startsWith(":")) {
            return RouteParameterValueType.SYMBOL;
        } else if (paramValue.startsWith("':")) {
            return RouteParameterValueType.SYMBOL_STRING_LITERAL;
        } else if (paramValue.startsWith("'")) {
            return RouteParameterValueType.STRING_LITERAL;
        } else if (paramValue.startsWith("[")) {
            return RouteParameterValueType.ARRAY;
        } else if (paramValue.startsWith("{")) {
            return RouteParameterValueType.HASH;
        } else {
            return RouteParameterValueType.UNKNOWN;
        }
    }

    RoutingParameterType detectParameterLabelType(String label) {
        if (label.startsWith(":") || label.endsWith(":") || wasHashParameter) {
            return RoutingParameterType.HASH;
        } else {
            return RoutingParameterType.IMPLICIT_PARAMETER;
        }
    }

    String cleanParameterString(String string) {
        if (string == null) {
            return null;
        }

        if (string.startsWith("'")) {
            string = string.substring(1);
        }
        if (string.endsWith("'")) {
            string = string.substring(0, string.length() - 1);
        }
        if (string.startsWith(":")) {
            string = string.substring(1);
        }
        if (string.endsWith(":")) {
            string = string.substring(0, string.length() - 1);
        }
        if (string.startsWith("=>")) {
            string = string.substring(2, string.length());
        }

        return string;
    }

    RailsAbstractParameter makeParameter(String parameterLabel, String parameterValue) {

        RailsAbstractParameter parameter = new RailsAbstractParameter();
        RouteParameterValueType parameterType = detectParameterValueType(parameterValue);
        parameter.setParameterType(parameterType);

        if (parameterLabel != null) {
            parameter.setLabelType(detectParameterLabelType(parameterLabel));
        }

        parameter.setValue(cleanParameterString(parameterValue));
        parameter.setLabel(cleanParameterString(parameterLabel));

        return parameter;

    }

    String parameterLabel = null;
    void processParametersPhase(int type, int lineNumber, String stringValue) {

        if (parameterLabel == null && type == '(') {
            //  Initializer parameters ie 'scope(path_name: { add: 'other', remove: 'other2' })'
            parsePhase = ParsePhase.INITIALIZER_PARAMETERS;
            return;
        }

        spansNextLine = ((type == '\n' && lastType == ',') || !isEntryScope());

        boolean isDoStatement = (stringValue != null && stringValue.equalsIgnoreCase("do") && isEntryScope());

        if ((type == '\n' && !spansNextLine) || isDoStatement) {
            parsePhase = ParsePhase.SEARCH_IDENTIFIER;

            if (wasRouteScope || isDoStatement) {
                scopeStack.add(currentDescriptor);
            }

            if (parameterLabel != null || (workingLine != null && workingLine.length() > 0)) {
                if (workingLine == null || workingLine.length() == 0) {
                    workingLine = parameterLabel;
                    parameterLabel = null;
                }

                if (parameterLabel != null && !parameterLabel.endsWith(":") && !wasHashParameter) {
                    //  Two values to be treated as separate parameters
                    currentDescriptor.addParameter(makeParameter(null, parameterLabel));
                    currentDescriptor.addParameter(makeParameter(null, workingLine));
                } else {
                    currentDescriptor.addParameter(makeParameter(parameterLabel, workingLine));
                }
            }

            wasRouteScope = false;
            wasHashParameter = false;
            currentDescriptor = null;
            parameterLabel = null;
            workingLine = "";
            return;
        }

        if ((type == ',' || (stringValue != null && lastString.endsWith(":"))) && isEntryScope()) {
            if (workingLine.length() == 0) {
                workingLine = parameterLabel;
                parameterLabel = null;
            }

            if (workingLine != null && workingLine.endsWith(":") && stringValue != null) {
                parameterLabel = workingLine;
                workingLine = stringValue;
            }

            if (workingLine != null) {
                currentDescriptor.addParameter(makeParameter(parameterLabel, workingLine));
            }
            wasRouteScope = false;
            wasHashParameter = false;
            parameterLabel = null;
            workingLine = "";
        } else {
            if (stringValue != null && parameterLabel == null) {
                parameterLabel = stringValue;
            }
            else {
                if (stringValue != null && isEntryScope() && stringValue.equalsIgnoreCase("do")) {
                    wasRouteScope = true;
                } else {
                    workingLine += CodeParseUtil.buildTokenString(type, stringValue);
                }
            }
        }

        if (type == '>' && lastType == '=' && !isInString && numOpenBrace == 0) {
            wasHashParameter = true;
            if (workingLine.endsWith("=>")) {
                workingLine = workingLine.substring(0, workingLine.length() - 2);
            }
        }
    }

    void processInitializerParametersPhase(int type, int line, String stringValue) {
        if (isEntryScope()) { // Will occur exactly on the last closing paren ')'
            parsePhase = ParsePhase.PARAMETERS;
        }

        // TODO

    }
}
