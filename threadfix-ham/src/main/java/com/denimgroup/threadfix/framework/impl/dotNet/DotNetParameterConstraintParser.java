package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.impl.dotNet.classParsers.CSharpEventTokenizerConfigurator;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.ScopeTracker;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  Detects parameter constraints embedded in a route template, ie `/{controller}/{action}/{id:int}`
public class DotNetParameterConstraintParser implements EventBasedTokenizer {

    private ScopeTracker scopeTracker = new ScopeTracker();
    private Map<String, ParameterDataType> parameterTypes = map();

    public static Map<String, ParameterDataType> run(String routeTemplate)
    {
        DotNetParameterConstraintParser parser = new DotNetParameterConstraintParser();
        EventBasedTokenizerRunner.runString(routeTemplate, new CSharpEventTokenizerConfigurator(), parser);
        return parser.parameterTypes;
    }

    private enum ParameterConstraintState {
        SEARCH, PARAMETER_NAME, PARAMETER_CONSTRAINT
    }

    private ParameterConstraintState currentState = ParameterConstraintState.SEARCH;
    private String currentParameterName = null;
    private String currentParameterConstraint = null;
    private List<String> pendingParameterConstraints = list();

    private ParameterDataType constraintToDataType(String constraint) {
        if ("int".equals(constraint)) {
            return ParameterDataType.INTEGER;
        } else if ("alpha".equals(constraint)) {
            //  Alpha-numeric
            return ParameterDataType.STRING;
        } else if ("bool".equals(constraint)) {
            return ParameterDataType.BOOLEAN;
        } else if ("datetime".equals(constraint)) {
            return ParameterDataType.DATE_TIME;
        } else if ("guid".equals(constraint)) {
            return ParameterDataType.STRING;
        } else if ("decimal".equals(constraint) || "float".equals(constraint) || "double".equals(constraint)) {
            return ParameterDataType.DECIMAL;
        } else {
            return null;
        }
    }

    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        scopeTracker.interpretToken(type);
        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); i++) {
                int c = stringValue.charAt(i);
                scopeTracker.interpretToken(c);
            }

            if (type > 0) {
                scopeTracker.interpretToken(type);
            }
        }

        switch (currentState) {
            case SEARCH:
                if (scopeTracker.getNumOpenBrace() > 0) {
                    currentState = ParameterConstraintState.PARAMETER_NAME;
                }
                break;

            case PARAMETER_NAME:
                if (scopeTracker.getNumOpenBrace() == 0) {
                    currentState = ParameterConstraintState.SEARCH;
                    currentParameterName = null;
                } else if (type == ':') {
                    currentState = ParameterConstraintState.PARAMETER_CONSTRAINT;
                } else {
                    if (currentParameterName == null) {
                        currentParameterName = "";
                    }

                    currentParameterName += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;

            case PARAMETER_CONSTRAINT:
                if (scopeTracker.getNumOpenBrace() == 0) {
                    if (currentParameterConstraint != null) {
                        pendingParameterConstraints.add(currentParameterConstraint);
                    }

                    ParameterDataType detectedType = null;
                    for (String constraint : pendingParameterConstraints) {
                        detectedType = constraintToDataType(constraint);
                        if (detectedType != null) {
                            break;
                        }
                    }

                    if (detectedType != null) {
                        parameterTypes.put(currentParameterName, detectedType);
                    }
                    currentState = ParameterConstraintState.SEARCH;
                    currentParameterName = null;
                    currentParameterConstraint = null;
                    pendingParameterConstraints.clear();
                } else if (type == ':') {
                    pendingParameterConstraints.add(currentParameterConstraint);
                    currentParameterConstraint = stringValue;
                } else {
                    if (currentParameterConstraint == null) {
                        currentParameterConstraint = "";
                    }
                    currentParameterConstraint += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;
        }
    }
}
