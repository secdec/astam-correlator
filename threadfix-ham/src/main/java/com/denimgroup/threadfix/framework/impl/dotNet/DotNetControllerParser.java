////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.set;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetControllerParser implements EventBasedTokenizer {

    final DotNetControllerMappings mappings;

    public static final SanitizedLogger LOG = new SanitizedLogger(DotNetControllerParser.class);

    public static final Set<String> DOT_NET_BUILTIN_CONTROLLERS = set(
            "ApiController", "Controller", "HubController", "HubControllerBase", "AsyncController", "BaseController"
    );

    @Nonnull
    public static DotNetControllerMappings parse(@Nonnull File file) {
        DotNetControllerParser parser = new DotNetControllerParser(file);
        EventBasedTokenizerRunner.run(file, parser);
        return parser.mappings;
    }

    DotNetControllerParser(File file) {
        LOG.debug("Parsing controller mappings for " + file.getAbsolutePath());
        mappings = new DotNetControllerMappings(file.getAbsolutePath());
    }

    public boolean hasValidControllerMappings() {
        return mappings.hasValidMappings();
    }

    @Override
//    public boolean shouldContinue() {
//        return shouldContinue;
//    }
    public boolean shouldContinue() {
        return true;
    }

    enum State {
        START, NAMESPACE, OPEN_BRACKET, AREA, PUBLIC, CLASS, TYPE_SIGNATURE, BODY, PUBLIC_IN_BODY, ACTION_RESULT, IACTION_RESULT, IN_ACTION_SIGNATURE, AFTER_BIND_INCLUDE, DEFAULT_VALUE, IN_ACTION_BODY
    }

    enum AttributeState {
        START, OPEN_BRACKET, STRING, AREA
    }

    enum ParameterState {
        START, REQUEST, REQUEST_INDEXER, SESSION, SESSION_INDEXER, FILES, QUERY_STRING, QUERY_STRING_INDEXER, COOKIES, COOKIES_INDEXER
    }

    State currentState      = State.START;
    AttributeState currentAttributeState = AttributeState.START;
    ParameterState currentParameterState = ParameterState.START;
    Set<String> currentAttributes = set();
    String lastAttribute;
    int   currentCurlyBrace = 0, currentParen = 0, classBraceLevel = 0,
            methodBraceLevel = 0, storedParen = 0, methodLineNumber = 0;
    boolean shouldContinue = true, wasDefaultValue = false;
    String  lastString     = null, methodName = null, twoStringsAgo = null, threeStringsAgo = null;
    Set<RouteParameter> parametersWithTypes = set();
    int lastLineNumber = -1;
    String possibleParamType = null;
    String currentNamespace = null;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        if (lineNumber != lastLineNumber) {
            lastLineNumber = lineNumber;
        }

        processMainThread(type, lineNumber, stringValue);
        processAttributes(type, stringValue);
        processRequestDataReads(type, stringValue);

        if (stringValue != null) {
            threeStringsAgo = twoStringsAgo;
            twoStringsAgo = lastString;
            lastString = stringValue;
        }
    }

    private void processMainThread(int type, int lineNumber, String stringValue) {

        switch (type) {
            case '{':
                currentCurlyBrace += 1;
                break;
            case '}':
                currentCurlyBrace -= 1;
                break;
            case '(':
                currentParen += 1;
                break;
            case ')':
                currentParen -= 1;
                break;
        }

        switch (currentState) {
            case START:
                if (NAMESPACE.equals(stringValue)) {
                    currentState = State.NAMESPACE;
                }else if(PUBLIC.equals(stringValue)){
                    currentState = State.PUBLIC;
                }
                break;
            case NAMESPACE:
                if(PUBLIC.equals(stringValue)){
                    currentState = State.PUBLIC;
                }else if( type == '['){
                    currentState = State.OPEN_BRACKET;
                }else {
                    if (currentNamespace == null) {
                        currentNamespace = "";
                    }
                    if (currentCurlyBrace == 0)
                        currentNamespace += CodeParseUtil.buildTokenString(type, stringValue);
                }
                break;
            case OPEN_BRACKET:
                if(stringValue != null && AREA.equalsIgnoreCase(stringValue)){
                    currentState = State.AREA;
                } else if(type == ']'){
                    currentState = State.NAMESPACE;
                }
                break;
            case AREA:
                if(PUBLIC.equals(stringValue)){
                    currentState = State.PUBLIC;
                } else if(stringValue != null && type != '(' && type != ')'){
                    mappings.setAreaName(stringValue);
                    currentState = State.START;
                }
                break;
            case PUBLIC:
                currentState = CLASS.equals(stringValue) ?
                        State.CLASS :
                        State.START;
                break;
            case CLASS:
                if (stringValue != null && stringValue.endsWith("Controller") &&
                        // Make sure we're not parsing internal ASP.NET MVC controller classes
                        !DOT_NET_BUILTIN_CONTROLLERS.contains(stringValue)) {
                    String controllerName = stringValue.substring(0, stringValue.indexOf("Controller"));
                    LOG.debug("Got Controller name " + controllerName);
                    mappings.setControllerName(controllerName);
                    mappings.setNamespace(currentNamespace);
                }

                currentState = State.TYPE_SIGNATURE;
                break;
            case TYPE_SIGNATURE:
                if (type == '{') {
                    currentState = State.BODY;
                    classBraceLevel = currentCurlyBrace - 1;
                }
                break;
            case BODY:
                if (classBraceLevel == currentCurlyBrace) {
                    shouldContinue = false;
                } else if (PUBLIC.equals(stringValue)) {
                    currentState = State.PUBLIC_IN_BODY;
                }
                break;
            case PUBLIC_IN_BODY:
                if (RESULT_TYPES.contains(stringValue) || hasHttpAttribute()) {
                    currentState = State.ACTION_RESULT;
                } else if (type == '(' || type == ';' || type == '{') {
                    currentState = State.BODY;
                    currentAttributes.clear();
                }
                break;
            case ACTION_RESULT:
                if (stringValue != null) {
                    lastString = stringValue;
                } else if (type == '(') {
                    assert lastString != null;

                    methodName = lastString;
                    lastString = null;
                    methodLineNumber = lineNumber;
                    storedParen = currentParen - 1;
                    lastString = null;
                    currentState = State.IN_ACTION_SIGNATURE;
                }

                break;
            case IN_ACTION_SIGNATURE:
                if (stringValue == null) {
                    if (type == ',' || type == ')' && lastString != null) {
                        if (isValidParameterName(lastString)) {
                            String name, dataType;
                            if (wasDefaultValue) {
                                name = twoStringsAgo;
                                dataType = threeStringsAgo;
                            } else {
                                name = lastString;
                                dataType = twoStringsAgo;
                            }

                            RouteParameter param = new RouteParameter(name);
                            param.setDataType(dataType);
                            parametersWithTypes.add(param);
                        }
                        if (twoStringsAgo.equals("Include")) {
                            currentState = State.AFTER_BIND_INCLUDE;
                        }

                        wasDefaultValue = false;
                    } else if (type == '=' && !"Include".equals(lastString)) {
                        currentState = State.DEFAULT_VALUE;
                    }
                } else if (lastString != null && lastString.equals("Include")) {
                    String paramNames = CodeParseUtil.trim(stringValue, "\"");
                    String[] paramNameParts = StringUtils.split(paramNames, ',');

                    for (String paramName : paramNameParts) {
                        paramName = paramName.trim();
                        RouteParameter param = new RouteParameter(paramName);
                        param.setParamType(RouteParameterType.FORM_DATA);
                        parametersWithTypes.add(param);
                    }
                }

                if (currentParen == storedParen) {
                    currentState = State.IN_ACTION_BODY;
                    methodBraceLevel = currentCurlyBrace;
                }
                break;
            case DEFAULT_VALUE:
                wasDefaultValue = true;
                if (stringValue != null) {
                    currentState = State.IN_ACTION_SIGNATURE;
                }
                break;
            case AFTER_BIND_INCLUDE:
                if (type == ',') {
                    currentState = State.IN_ACTION_SIGNATURE;
                }

                if (type == ')' && currentParen == storedParen) {
                    currentState = State.IN_ACTION_BODY;
                    methodBraceLevel = currentCurlyBrace;
                }
                break;
            case IN_ACTION_BODY:
                if (currentCurlyBrace == methodBraceLevel) {
                    mappings.addAction(
                            methodName, currentAttributes, methodLineNumber,
                            lineNumber, parametersWithTypes);
                    currentAttributes = set();
                    parametersWithTypes = set();
                    methodName = null;
                    currentState = State.BODY;
                }
                break;
        }

    }

    private void processAttributes(int type, String stringValue) {
        if (currentState == State.BODY && currentCurlyBrace == methodBraceLevel) {
            switch (currentAttributeState) {
                case START:
                    if (type == '[') {
                        currentAttributeState = AttributeState.OPEN_BRACKET;
                    }
                    break;
                case OPEN_BRACKET:
                    if (stringValue != null) {
                        lastAttribute = stringValue;
                        currentAttributeState = AttributeState.STRING;
                    }
                    break;
                case STRING:
                    boolean addAttribute = false;
                    if (type == ']') {
                        currentAttributeState = AttributeState.START;
                        addAttribute = true;
                    }

                    if (type == ',') {
                        addAttribute = true;
                        currentAttributeState = AttributeState.OPEN_BRACKET;
                    }

                    if (addAttribute) {
                        LOG.debug("Adding " + lastAttribute);
                        currentAttributes.add(lastAttribute);
                    }
                    break;
            }
        }
    }

    private void processRequestDataReads(int type, String stringValue) {
        if (currentState != State.IN_ACTION_BODY) {
            return;
        }

        switch (currentParameterState) {
            case START:
                possibleParamType = null;

                if ("Request.Files".equals(stringValue)) {
                    RouteParameter param = new RouteParameter("[File Data]");
                    param.setParamType(RouteParameterType.FILES);
                    parametersWithTypes.add(param);
                } else if ("Request.Cookies".equals(stringValue)) {
                    currentParameterState = ParameterState.COOKIES;
                } else if ("Request.QueryString".equals(stringValue)) {
                    currentParameterState = ParameterState.QUERY_STRING;
                } else if ("Request".equals(stringValue)) {
                    currentParameterState = ParameterState.REQUEST;
                } else if ("Session".equals(stringValue)) {
                    currentParameterState = ParameterState.SESSION;
                }

                if (currentParameterState != ParameterState.START) {
                    if (twoStringsAgo != null && ParameterDataType.getType(twoStringsAgo).getDisplayName() != null) {
                        possibleParamType = twoStringsAgo;
                    } else if (lastString != null && ParameterDataType.getType(lastString).getDisplayName() != null) {
                        possibleParamType = lastString;
                    } else {
                        possibleParamType = null;
                    }
                }
                break;

            case REQUEST:
                if (type == '[') {
                    currentParameterState = ParameterState.REQUEST_INDEXER;
                } else {
                    currentParameterState = ParameterState.START;
                }
                break;

            case REQUEST_INDEXER:
                if (type == '"' && stringValue != null) {
                    RouteParameter param = new RouteParameter(stringValue);
                    param.setParamType(RouteParameterType.UNKNOWN);
                    param.setDataType(possibleParamType);
                    currentParameterState = ParameterState.START;
                } else {
                    currentParameterState = ParameterState.START;
                }
                break;

            case QUERY_STRING:
                if (type == '[') {
                    currentParameterState = ParameterState.QUERY_STRING_INDEXER;
                } else {
                    currentParameterState = ParameterState.START;
                }
                break;

            case QUERY_STRING_INDEXER:
                if (stringValue != null && type == '"') {
                    RouteParameter param = new RouteParameter(stringValue);
                    param.setDataType(possibleParamType);
                    param.setParamType(RouteParameterType.QUERY_STRING);
                    parametersWithTypes.add(param);
                }
                currentParameterState = ParameterState.START;
                break;

            case COOKIES:
                if (type == '[') {
                    currentParameterState = ParameterState.COOKIES_INDEXER;
                } else {
                    currentParameterState = ParameterState.START;
                }
                break;

            case COOKIES_INDEXER:
                if (type == '"' && stringValue != null) {
                    RouteParameter param = new RouteParameter(stringValue);
                    param.setDataType(possibleParamType);
                    param.setParamType(RouteParameterType.COOKIE);
                    parametersWithTypes.add(param);
                }

                currentParameterState = ParameterState.START;
                break;

            case SESSION:
                if (type == '[') {
                    currentParameterState = ParameterState.SESSION_INDEXER;
                } else {
                    currentParameterState = ParameterState.START;
                }
                break;

            case SESSION_INDEXER:
                if (type == '"' && stringValue != null) {
                    RouteParameter param = new RouteParameter(stringValue);
                    param.setDataType(possibleParamType);
                    param.setParamType(RouteParameterType.SESSION);
                    parametersWithTypes.add(param);
                }
                currentParameterState = ParameterState.START;
                break;
        }
    }

    private boolean hasHttpAttribute() {
        for (String attr : currentAttributes) {
            if ("HttpGet".equals(attr) ||
                    "HttpPost".equals(attr) ||
                    "HttpPatch".equals(attr) ||
                    "HttpPut".equals(attr) ||
                    "HttpDelete".equals(attr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidParameterName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 48) {
                return false;
            }
            if (c > 57 && c < 65) {
                return false;
            }
            if (c > 90 && c < 97 && c != '_') {
                return false;
            }
            if (c > 122) {
                return false;
            }
        }
        return true;
    }
}