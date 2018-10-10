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
