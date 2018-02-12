package com.denimgroup.threadfix.framework.impl.struts.model;

import org.apache.commons.io.FileUtils;
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
                            action = valueMatcher.replaceFirst("");
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
                    if (currentTargetMethod == null) {
                        currentTargetMethod = "GET";
                    }
                }

            } else if (cleanedName.equals("url")) {

                String id = htmlNode.attr("id");
                String value = htmlNode.attr("value");
                if (id != null && value != null) {
                    symbolEndpointMap.put(id, value);
                }

            } else {

                String name = htmlNode.attr("key");

                if (currentTargetUrl != null) {
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
