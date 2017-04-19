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
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;

import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.*;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetRoutesParser implements EventBasedTokenizer {

    DotNetRouteMappings mappings = new DotNetRouteMappings();

    public static final SanitizedLogger LOG = new SanitizedLogger(DotNetRoutesParser.class);

    public static final boolean logParsing = false;

    public boolean hasValidMappings() {
        return !mappings.routes.isEmpty();
    }

    @Nonnull
    public static DotNetRouteMappings parse(@Nonnull File file) {
        DotNetRoutesParser parser = new DotNetRoutesParser();
        EventBasedTokenizerRunner.run(file, parser);
        return parser.mappings;
    }

    @Override
    public boolean shouldContinue() {
        return true; // TODO determine end condition
    }

    private void log(Object string) {
        if (logParsing && string != null) {
            LOG.debug(string.toString());
        }
    }

    enum Phase {
        IDENTIFICATION, IN_CLASS, IN_METHOD
    }

    Phase               currentPhase               = Phase.IDENTIFICATION;
    IdentificationState currentIdentificationState = IdentificationState.START;
    ClassBodyState      currentClassBodyState      = ClassBodyState.START;
    MapRouteState       currentMapRouteState       = MapRouteState.START;

    int parenCount = 0;

    @Override
    public void processToken(int type, int lineNumber, String stringValue) {

        log("type  : " + type);
        log("string: " + stringValue);

        if (type == '(') {
            parenCount += 1;
            log("Paren count: " + parenCount);
            log("Paren current: " + currentParenCount);
        } else if (type == ')') {
            parenCount -= 1;
            log("Paren count: " + parenCount);
            log("Paren current: " + currentParenCount);
        }

        log("phase: " + currentPhase + " ");

        switch (currentPhase) {
            case IDENTIFICATION:
                processIdentification(stringValue);
                break;
            case IN_CLASS:
                processClass(type, stringValue);
                break;
            case IN_METHOD:
                processMethod(type, stringValue);
        }

    }

    enum IdentificationState {
        START, PUBLIC, CLASS, ROUTE_CONFIG, STARTUP, COLON,
    }
    private void processIdentification(String stringValue) {
        log(currentIdentificationState);
        switch (currentIdentificationState) {
            case START:
                if (PUBLIC.equals(stringValue)) {
                    currentIdentificationState = IdentificationState.PUBLIC;
                } else if (":".equals(stringValue)) {
                    currentIdentificationState = IdentificationState.COLON;
                }
                break;
            case PUBLIC:
                currentIdentificationState = CLASS.equals(stringValue) ?
                        IdentificationState.CLASS :
                        IdentificationState.START;
                break;
            case CLASS:
                if(ROUTE_CONFIG.equals(stringValue))
                    currentIdentificationState = IdentificationState.ROUTE_CONFIG;
                else if(STARTUP.equals(stringValue))
                    currentIdentificationState = IdentificationState.STARTUP;
                else
                    currentIdentificationState = IdentificationState.CLASS;
                break;
            case ROUTE_CONFIG:
                currentPhase = Phase.IN_CLASS;
                break;
            case STARTUP:
                currentPhase = Phase.IN_CLASS;
                break;
            case COLON:
                if (SYSTEM_HTTP_APPLICATION.equals(stringValue)) {
                    currentPhase = Phase.IN_CLASS;
                } else {
                    currentIdentificationState = IdentificationState.START;
                }
                break;
        }
    }

    int currentParenCount = -1;
    String variableName = null; // this is the name of the RouteCollection variable
    enum ClassBodyState {
        START, METHOD_SIGNATURE, ROUTE_COLLECTION,IAPPLICATION_BUILDER, METHOD_BODY, MAP_ROUTE
    }

    private void processClass(int type, String stringValue) {
        log(currentClassBodyState);

        switch (currentClassBodyState) {
            case START:
                if (REGISTER_ROUTES.equals(stringValue)) {
                    currentClassBodyState = ClassBodyState.METHOD_SIGNATURE;
                    currentParenCount = parenCount;
                } else if(CONFIGURE.equals(stringValue)){
                    currentClassBodyState = ClassBodyState.METHOD_SIGNATURE;
                    currentParenCount = parenCount;
                }
                break;
            case METHOD_SIGNATURE:
                if (ROUTE_COLLECTION.equals(stringValue)) {
                    currentClassBodyState = ClassBodyState.ROUTE_COLLECTION;
                } else if(IAPPLICATION_BUILDER.equals(stringValue)){
                    currentClassBodyState = ClassBodyState.IAPPLICATION_BUILDER;
                }

                if (type == ')' && currentParenCount == parenCount) {
                    if (variableName == null) {
                        currentClassBodyState = ClassBodyState.START;
                    } else {
                        currentClassBodyState = ClassBodyState.METHOD_BODY;
                    }
                }
                break;
            case ROUTE_COLLECTION:
                variableName = stringValue;
                currentClassBodyState = ClassBodyState.METHOD_SIGNATURE;
                break;
            case IAPPLICATION_BUILDER:
                variableName = stringValue;
                currentClassBodyState = ClassBodyState.METHOD_SIGNATURE;
                break;
            case METHOD_BODY:
                assert variableName != null;

                if (stringValue != null && stringValue.equals(variableName + ".MapRoute")) {
                    currentClassBodyState = ClassBodyState.MAP_ROUTE;
                } else if (stringValue != null && stringValue.equals("routes.MapRoute")){
                    currentClassBodyState = ClassBodyState.MAP_ROUTE;
                }
                break;
            case MAP_ROUTE:

                currentParenCount = parenCount - 1;

                log("Paren count: " + parenCount);
                log("Paren current: " + currentParenCount);

                currentClassBodyState = ClassBodyState.METHOD_BODY;
                currentPhase = Phase.IN_METHOD;

                break;

        }

    }

    String currentUrl = null,
            currentName = null,
            currentDefaultController = null,
            currentDefaultAction = null,
            parameterName = null,
            parameterValue = null;
    // TODO split this up
    enum MapRouteState { // these states are to be used with the IN_METHOD Phase
        START, URL, URL_COLON, NAME, NAME_COLON, DEFAULTS,
        DEFAULTS_COLON, DEFAULTS_NEW, DEFAULTS_OBJECT, DEFAULTS_CONTROLLER, DEFAULTS_CONTROLLER_EQUALS,
        DEFAULTS_ACTION, DEFAULTS_ACTION_EQUALS, DEFAULTS_PARAM, DEFAULTS_PARAM_EQUALS, TEMPLATE, TEMPLATE_COLON
    }

    int commaCount = 0;

    private void processMethod(int type, String stringValue) {
        log(currentMapRouteState);

        if (type == ',') {
            commaCount ++;
            log("Comma count is " + commaCount);
        }

        switch (currentMapRouteState) {
            case START:
                if (URL.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.URL;
                } else if ((URL + ":").equals(stringValue)) {
                    currentMapRouteState = MapRouteState.URL_COLON;
                } else if (NAME.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.NAME;
                } else if ((NAME + ":").equals(stringValue)) {
                    currentMapRouteState = MapRouteState.NAME_COLON;
                } else if (DEFAULTS.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.DEFAULTS;
                } else if ((DEFAULTS + ":").equals(stringValue)) {
                    currentMapRouteState = MapRouteState.DEFAULTS_COLON;
                } else if ('"' == type) {
                    if (commaCount == 0) {
                        currentName = stringValue; // name
                    } else if (commaCount == 1) {
                        currentUrl = stringValue; // url
                    }
                } else if (NEW.equals(stringValue) && commaCount == 2) {
                    currentMapRouteState = MapRouteState.DEFAULTS_NEW;
                } else if (TEMPLATE.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.TEMPLATE;
                } else if ((TEMPLATE + ":").equals(stringValue)){
                    currentMapRouteState = MapRouteState.TEMPLATE_COLON;
                }
                break;

            case URL:
                if (type == ':') {
                    currentMapRouteState = MapRouteState.URL_COLON;
                }
                break;
            case URL_COLON:
                currentUrl = stringValue;
                currentMapRouteState = MapRouteState.START;
                break;
            case NAME:
                if (type == ':') {
                    currentMapRouteState = MapRouteState.NAME_COLON;
                }
                break;
            case NAME_COLON:
                currentName = stringValue;
                currentMapRouteState = MapRouteState.START;
                break;
            case DEFAULTS:
                if (type == ':') {
                    currentMapRouteState = MapRouteState.DEFAULTS_COLON;
                }
                break;
            case DEFAULTS_COLON:
                if (NEW.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.DEFAULTS_NEW;
                }
                break;
            case DEFAULTS_NEW:
                if ('{' == type) {
                    currentMapRouteState = MapRouteState.DEFAULTS_OBJECT;
                }
                break;
            case DEFAULTS_OBJECT:
                if (CONTROLLER.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.DEFAULTS_CONTROLLER;
                } else if (ACTION.equals(stringValue)) {
                    currentMapRouteState = MapRouteState.DEFAULTS_ACTION;
                } else if (stringValue != null) {
                    parameterName = stringValue;
                    currentMapRouteState = MapRouteState.DEFAULTS_PARAM;
                } else if ('}' == type) {
                    currentMapRouteState = MapRouteState.START;
                }
                break;
            case DEFAULTS_CONTROLLER:
                if ('=' == type) {
                    currentMapRouteState = MapRouteState.DEFAULTS_CONTROLLER_EQUALS;
                }
                break;
            case DEFAULTS_CONTROLLER_EQUALS:
                currentDefaultController = stringValue;
                currentMapRouteState = MapRouteState.DEFAULTS_OBJECT;
                break;
            case DEFAULTS_ACTION:
                if ('=' == type) {
                    currentMapRouteState = MapRouteState.DEFAULTS_ACTION_EQUALS;
                }
                break;
            case DEFAULTS_ACTION_EQUALS:
                currentDefaultAction = stringValue;
                currentMapRouteState = MapRouteState.DEFAULTS_OBJECT;
                break;
            case DEFAULTS_PARAM:
                if ('=' == type) {
                    currentMapRouteState = MapRouteState.DEFAULTS_PARAM_EQUALS;
                }
                break;
            case DEFAULTS_PARAM_EQUALS:
                parameterValue = stringValue;
                currentMapRouteState = MapRouteState.DEFAULTS_OBJECT;
                break;
            case TEMPLATE:
                currentMapRouteState = MapRouteState.TEMPLATE_COLON;
                break;
            case TEMPLATE_COLON:
                if(stringValue == null){
                    currentMapRouteState = MapRouteState.TEMPLATE_COLON;
                    break;
                }

                String[] templateSections = stringValue.split("/");
                String openBracket = "{";
                String closeBracket ="}";
                String equals = "=";
                StringBuilder urlPattern = new StringBuilder("");

                for(int i = 0; i < templateSections.length; i++){
                    String section, patternSection;
                    patternSection = section = templateSections[i];
                    String param, value;

                    if( !section.contains(openBracket) && !section.contains(closeBracket)){
                        urlPattern.append(section + "/");
                        continue;
                    }

                    if(section.contains(equals)){
                        param = section.substring(section.indexOf(openBracket) + 1, section.indexOf(equals));
                        value = section.substring(section.indexOf(equals) + 1, section.indexOf(closeBracket));
                    } else {
                        param = section.substring(section.indexOf(openBracket) +1, section.indexOf(closeBracket));
                        value = "";
                    }

                    if("controller".equalsIgnoreCase(param)){
                        currentDefaultController = value;
                        patternSection = section.replace(section.substring(section.lastIndexOf(param) + param.length(), section.indexOf("}")), "");
                    } else if ("action".equalsIgnoreCase(param)){
                        patternSection = section.replace(section.substring(section.lastIndexOf(param) + param.length() , section.indexOf("}")), "");
                        currentDefaultAction = value;
                    } else {
                        parameterName = param.replaceAll("[^A-Za-z0-9]+", "");
                        parameterValue = value;
                        patternSection = section.replace(section.substring(section.lastIndexOf(parameterName) + parameterName.length(), section.indexOf("}")), "");
                    }

                    urlPattern.append(patternSection + "/");
                }
                urlPattern = urlPattern.deleteCharAt(urlPattern.length() - 1);
                currentUrl = urlPattern.toString();
                currentMapRouteState = MapRouteState.START;
                break;

        }

        if (parenCount == currentParenCount) {
            log("Paren count: " + parenCount);
            log("Paren current: " + currentParenCount);
            mappings.addRoute(currentName, currentUrl, currentDefaultController, currentDefaultAction, parameterName);
            commaCount = 0;
            currentPhase = Phase.IN_CLASS;
        }

    }


    }


