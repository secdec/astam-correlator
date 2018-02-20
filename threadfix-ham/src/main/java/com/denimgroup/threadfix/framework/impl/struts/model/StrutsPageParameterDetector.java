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

package com.denimgroup.threadfix.framework.impl.struts.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class StrutsPageParameterDetector {

    Pattern interpolatedStringPattern = Pattern.compile("%\\{([^\\}]+)\\}");

    public List<StrutsDetectedParameter> parseStrutsFormsParameters(File htmlOrJspFile) {

        final Map<String, String> symbolToEndpointMap = map();

        Document doc;
        try {
            String fileContents = FileUtils.readFileToString(htmlOrJspFile);
            doc = Jsoup.parse(fileContents);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (doc == null) {
            return null;
        }

        List<StrutsDetectedParameter> params = list();

        for (Node node : doc.childNodes()) {
            params.addAll(parseNode(node, symbolToEndpointMap, null, null));
        }

        for (StrutsDetectedParameter param : params) {
            param.sourceFile = htmlOrJspFile.getAbsolutePath();
        }

        return params;
    }

    private List<StrutsDetectedParameter> parseNode(Node htmlNode, Map<String, String> symbolEndpointMap, String currentTargetUrl, String currentTargetMethod) {

        List<StrutsDetectedParameter> result = list();

        if (htmlNode.nodeName().contains(":")) {
            String cleanedName = htmlNode.nodeName().split(":")[1];
            if (cleanedName.equals("form")) {

                String action = htmlNode.attr("action");
                if (action != null) {
                    Matcher valueMatcher = interpolatedStringPattern.matcher(action);
                    while (valueMatcher.find()) {
                        String varName = valueMatcher.group(1);
                        if (varName.equals("#request.contextPath")) {
                            int replaceStart = valueMatcher.start();
                            int replaceEnd = valueMatcher.end();
                            if (action.length() != replaceEnd && action.charAt(replaceEnd) == '/') {
                                replaceEnd++;
                            }
                            action = action.substring(0, replaceStart) + action.substring(replaceEnd);
                        } else {
                            String resolvedValue = symbolEndpointMap.get(varName);
                            if (resolvedValue != null) {
                                action = valueMatcher.replaceFirst(resolvedValue);
                            } else {
                                action = valueMatcher.replaceFirst("{" + varName + "}");
                            }
                        }

                        valueMatcher = interpolatedStringPattern.matcher(action);
                    }

                    action = action.replaceAll("%", "");

                    currentTargetUrl = action;
                    currentTargetMethod = htmlNode.attr("method");
                    if (currentTargetMethod == null || currentTargetMethod.isEmpty()) {
                        currentTargetMethod = "POST";
                    }
                }

            } else if (cleanedName.equals("url")) {

                String id = htmlNode.attr("id");
                String value = htmlNode.attr("value");
                if (id != null && value != null) {
                    symbolEndpointMap.put(id, value);
                }
            } else if (cleanedName.equals("file")) {

                String name = htmlNode.attr("name");
                if (name == null || name.isEmpty()) {
                    name = "[File]";
                }
                if (currentTargetUrl != null) {
                    StrutsDetectedParameter param = new StrutsDetectedParameter();
                    param.targetEndpoint = currentTargetUrl;
                    param.queryMethod = currentTargetMethod;
                    param.paramName = name;
                    result.add(param);
                }

            } else {

                String name = htmlNode.attr("key");
                if (name == null || name.isEmpty()) {
                    name = htmlNode.attr("name");
                }

                if (currentTargetUrl != null && name != null && !name.isEmpty()) {
                    StrutsDetectedParameter param = new StrutsDetectedParameter();
                    param.paramName = name;
                    param.targetEndpoint = currentTargetUrl;
                    param.queryMethod = currentTargetMethod;
                    result.add(param);
                }

            }
        }

        for (Node child : htmlNode.childNodes()) {
            result.addAll(parseNode(child, symbolEndpointMap, currentTargetUrl, currentTargetMethod));
        }

        return result;
    }

}
