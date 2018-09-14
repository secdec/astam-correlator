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
