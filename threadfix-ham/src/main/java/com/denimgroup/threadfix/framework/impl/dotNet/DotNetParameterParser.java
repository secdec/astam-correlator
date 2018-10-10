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

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

public class DotNetParameterParser implements EventBasedTokenizer {

    private RouteParameterMap parsedParameters = new RouteParameterMap();

    public RouteParameterMap getParsedParameterReferences() {
        return parsedParameters;
    }



    private enum ParameterState {
        SEARCH, WAITING_INDEXER, INDEXER
    }

    private ParameterState currentParameterState = ParameterState.SEARCH;
    private RouteParameter pendingParameter = null;
    private String[] stringHistory = new String[2];



    @Override
    public boolean shouldContinue() {
        return true;
    }

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {
        switch (currentParameterState) {
            case SEARCH:
                if (stringValue == null) {
                    break;
                }

                if ("this".equals(stringValue)) {
                    break;
                }

                if (stringValue.startsWith("this.")) {
                    stringValue = stringValue.substring("this.".length());
                }

                if (stringValue.startsWith("HttpContext.Current.")) {
                    stringValue = stringValue.substring("HttpContext.Current.".length());
                }

                if (REQUEST.equals(stringValue)) {
                    pendingParameter = new RouteParameter();
                    pendingParameter.setDataType(stringHistory[1]);
                    currentParameterState = ParameterState.WAITING_INDEXER;
                } else if (REQUEST_FILES.equals(stringValue)) {
                    RouteParameter fileParameter = new RouteParameter("[File Data]");
                    fileParameter.setParamType(RouteParameterType.FILES);
                    parsedParameters.put(lineNumber, fileParameter);
                } else if (REQUEST_COOKIES.equals(stringValue)) {
                    pendingParameter = new RouteParameter();
                    pendingParameter.setDataType(stringHistory[1]);
                    pendingParameter.setParamType(RouteParameterType.COOKIE);
                } else if (REQUEST_QUERY_STRING.equals(stringValue) || REQUEST_QUERY.equals(stringValue)) {
                    pendingParameter = new RouteParameter();
                    pendingParameter.setDataType(stringHistory[1]);
                    pendingParameter.setParamType(RouteParameterType.QUERY_STRING);
                } else if (SESSION.equals(stringValue)) {
                    pendingParameter = new RouteParameter();
                    pendingParameter.setDataType(stringHistory[1]);
                    pendingParameter.setParamType(RouteParameterType.SESSION);
                }

                if (pendingParameter != null) {
                    currentParameterState = ParameterState.WAITING_INDEXER;
                }

                break;

            case WAITING_INDEXER:
                if (type == '[') {
                    currentParameterState = ParameterState.INDEXER;
                } else {
                    currentParameterState = ParameterState.SEARCH;
                    pendingParameter = null;
                }
                break;

            case INDEXER:
                if (stringValue != null && type == '"') {
                    pendingParameter.setName(stringValue);
                    parsedParameters.put(lineNumber, pendingParameter);
                }
                pendingParameter = null;
                currentParameterState = ParameterState.SEARCH;
                break;
        }

        if (stringValue != null) {
            for (int i = stringHistory.length - 1; i > 0; i--) {
                stringHistory[i] = stringHistory[i - 1];
            }

            stringHistory[0] = stringValue;
        }
    }
}
