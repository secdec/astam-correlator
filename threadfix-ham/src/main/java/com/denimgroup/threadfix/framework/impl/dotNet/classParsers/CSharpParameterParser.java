package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.*;

public class CSharpParameterParser extends AbstractCSharpParser<CSharpParameter> implements EventBasedTokenizer {

    private CSharpAttributeParser attributeParser;
    private CSharpScopeTracker scopeTracker;

    @Override
    public void setParsingContext(CSharpParsingContext context) {
        attributeParser = context.getAttributeParser();
        scopeTracker = context.getScopeTracker();
    }

    @Override
    public void reset() {
        super.reset();
        currentParameterState = ParameterState.SEARCH;
    }


    private enum ParameterState {
        SEARCH, PARAMETER_START, DEFAULT_VALUE, EXPLICIT_VALUE
    }

    private ParameterState currentParameterState = ParameterState.SEARCH;
    private String workingString = null;
    private int workingParameterIndex = -1;

    public boolean isBuildingParameterType() {
        return currentParameterState == ParameterState.PARAMETER_START && isValidPartialTypeName(workingString);
    }

    private void finalizeParameter(boolean makeNew) {
        workingString = null;
        finalizePendingItem();
        if (makeNew) {
            CSharpParameter newParameter = new CSharpParameter();
            newParameter.setParameterIndex(++workingParameterIndex);
            setPendingItem(newParameter);
        } else {
            workingParameterIndex = -1;
        }
    }

    private boolean pendingParameterHasData() {
        CSharpParameter pending = getPendingItem();
        return pending != null && (
            pending.getType() != null ||
            pending.getName() != null ||
            pending.getDefaultValue() != null ||
            pending.getValue() != null
        );
    }

    private void setCurrentState(ParameterState newState) {
        workingString = null;
        currentParameterState = newState;
    }


    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        assert this.attributeParser != null : "setAttributeParser must be called before running CSharpParameterParser!";

        if (isDisabled() || scopeTracker.isInComment()) {
            return;
        }

        switch (currentParameterState) {
            case SEARCH:
                if (scopeTracker.getNumOpenParen() > 1) {
                    break;
                }

                if (type == '(') {
                    setCurrentState(ParameterState.PARAMETER_START);
                    setPendingItem(new CSharpParameter());
                }
                break;

                //  TODO - Break this up
            case PARAMETER_START:
                CSharpParameter pendingParameter = getPendingItem();

                while (!attributeParser.isBuildingItem() && attributeParser.hasItem()) {
                    pendingParameter.addAttribute(attributeParser.pullCurrentItem());
                }

                if (scopeTracker.getNumOpenParen() == 0) {
                    if (pendingParameterHasData()) {
                        if (workingString != null) {
                            if (pendingParameter.getType() != null) {
                                pendingParameter.setName(workingString);
                            } else {
                                pendingParameter.setValue(workingString);
                            }
                        }

                        finalizeParameter(false);
                    } else if (workingString != null) {
                        pendingParameter.setValue(workingString);
                        finalizeParameter(false);
                    } else {
                        setPendingItem(null);
                    }

                    setCurrentState(ParameterState.SEARCH);
                    break;
                }

                if (pendingParameter.getType() == null && (stringValue != null || scopeTracker.isInString() || scopeTracker.getNumOpenParen() > 1 || scopeTracker.getNumOpenAngleBracket() > 0)) {
                    //  Try to detect parameter data type for parameter declarations
                    if (workingString == null) {
                        workingString = "";
                    }

                    //  A bit of a messy way to detect spaces between valid type names to determine whether
                    //  a parameter type was just declared
                    if (stringValue != null && !workingString.isEmpty()) {
                        //  We have built a string and are getting another string

                        if (isValidTypeName(workingString)) {
                            pendingParameter.setType(workingString);
                            workingString = "";
                        } else if (isAttributeSyntax(workingString)) {
                            //  Attribute attached to a parameter, ignore it
                            workingString = "";
                        }
                    }

                    if ("this".equals(workingString)) {
                        pendingParameter.setIsExtensionParameter(true);
                        workingString = "";
                    } else if ("out".equals(stringValue) || "params".equals(stringValue)) {
                        workingString = "";
                        break;
                    }

                    workingString += CodeParseUtil.buildTokenString(type, stringValue);
                } else {
                    switch (type) {
                        case ',':
                            if (workingString != null) {
                                if (pendingParameter.getType() != null) {
                                    pendingParameter.setName(workingString);
                                } else {
                                    pendingParameter.setValue(workingString);
                                }
                                workingString = null;
                            }

                            finalizeParameter(true);
                            break;

                        case '=':
                            if (isValidVariableName(workingString)) {
                                pendingParameter.setName(workingString);
                                if (pendingParameter.getType() != null) {
                                    //  Default value
                                    setCurrentState(ParameterState.DEFAULT_VALUE);
                                } else {
                                    //  Explicitly assigned value for attribute parameters
                                    workingString = null;
                                }
                            } else {
                                //  Probably a lambda (next char would be '>')
                                if (workingString == null) {
                                    workingString = "";
                                }
                                workingString += CodeParseUtil.buildTokenString(type, stringValue);
                            }
                            break;

                        default:
                            if (type == ':') {
                                assert pendingParameter.getType() == null : "Expected a parameter value, not a declaration!";
                                pendingParameter.setName(workingString);
                                setCurrentState(ParameterState.EXPLICIT_VALUE);
                            } else {
                                if (workingString == null) {
                                    workingString = "";
                                }
                                workingString += CodeParseUtil.buildTokenString(type, stringValue);
                            }
                            break;
                    }
                }
                break;

            case DEFAULT_VALUE:
                if (workingString == null) {
                    workingString = "";
                }

                if (scopeTracker.getNumOpenParen() == 0) {
                    finalizeParameter(false);
                    setCurrentState(ParameterState.SEARCH);
                    break;
                }

                if (stringValue != null || scopeTracker.isInString() || scopeTracker.getNumOpenParen() > 1) {
                    workingString += CodeParseUtil.buildTokenString(type, stringValue);
                } else if (type == ',') {
                    getPendingItem().setDefaultValue(workingString);
                    finalizeParameter(true);
                    setCurrentState(ParameterState.PARAMETER_START);
                    break;
                }
                break;

            case EXPLICIT_VALUE:
                if (workingString == null) {
                    workingString = "";
                }

                if (scopeTracker.getNumOpenParen() == 0) {
                    finalizeParameter(false);
                    setCurrentState(ParameterState.SEARCH);
                    break;
                }

                if (stringValue != null || scopeTracker.isInString() || scopeTracker.getNumOpenParen() > 1) {
                    workingString += CodeParseUtil.buildTokenString(type, stringValue);
                } else if (type == ',') {
                    getPendingItem().setValue(workingString);
                    finalizeParameter(true);
                    setCurrentState(ParameterState.PARAMETER_START);
                    break;
                }
                break;
        }
    }
}
