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

package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class HyperlinkParameterDetector {

    private static final SanitizedLogger LOG = new SanitizedLogger(HyperlinkParameterDetector.class);

    public HyperlinkParameterDetector() {

    }

    private List<String> watchedElements = list("a", "input", "form", "textarea", "select", "option");

    public HyperlinkParameterDetectionResult parse(@Nonnull String html, @Nullable File sourceFile) {
        Stack<ElementReference> elementStack = new Stack<ElementReference>();
        List<ElementReference> rootElements = list();

        Document doc;

        try {
            doc = Jsoup.parse(html);
        } catch (Exception anyException) {
            anyException.printStackTrace();
            return null;
        }

        if (doc == null) {
            return null;
        }

        for (Node child : doc.childNodes()) {
            processNode(child, elementStack, rootElements);
        }

        if (sourceFile != null) {
            assignSourceFileToElements(rootElements, sourceFile.getAbsolutePath());
        }

        parseElementReferences(ElementReference.flattenReferenceTree(rootElements));

        Map<String, Map<String, List<RouteParameter>>> detectedParameters = parseReferenceParameters(rootElements);

        return new HyperlinkParameterDetectionResult(rootElements, detectedParameters);
    }

    private void processNode(Node htmlNode, Stack<ElementReference> elementStack, List<ElementReference> rootElements) {
        boolean pushedStack = false;

        String[] quoteTrimTokens = new String[] { "\"", "'" };

        if (watchedElements.contains(htmlNode.nodeName())) {

            ElementReference newElement = new ElementReference();
            newElement.setElementType(htmlNode.nodeName().toLowerCase());

            for (Attribute attr : htmlNode.attributes()) {
                newElement.addAttribute(CodeParseUtil.trim(attr.getKey(), quoteTrimTokens, 1).toLowerCase(), CodeParseUtil.trim(attr.getValue(), quoteTrimTokens, 1));
            }

            if (elementStack.isEmpty()) {
                rootElements.add(newElement);
            } else {
                elementStack.peek().addChild(newElement);
            }

            elementStack.push(newElement);

            pushedStack = true;
        }

        for (Node childNode : htmlNode.childNodes()) {
            processNode(childNode, elementStack, rootElements);
        }

        if (pushedStack) {
            elementStack.pop();
        }
    }

    private void assignSourceFileToElements(List<ElementReference> elements, String sourceFilePath) {
        Stack<ElementReference> pendingElements = new Stack<ElementReference>();
        pendingElements.addAll(elements);

        while (!pendingElements.isEmpty()) {
            ElementReference current = pendingElements.pop();
            current.setSourceFile(sourceFilePath);

            pendingElements.addAll(current.getChildren());
        }
    }

    private void parseElementReferences(List<ElementReference> references) {
        for (ElementReference reference : references) {
            if (reference.getElementType().equals("input")) {
                //  TODO - Detect "formaction" attribute and update containing form accordingly
                String type = reference.getAttributeValue("type");
                String name = reference.getAttributeValue("name");
                if ((type != null && (type.equals("submit") || type.equals("reset"))) || name == null || name.trim().length() == 0) {
                    continue;
                }
                reference.addRequestParameter(name);

            } else if (reference.getElementType().equals("form")) {
                String action = reference.getAttributeValue("action");
                if (action == null) {
                    continue;
                }
                String method = reference.getAttributeValue("method");
                if (method == null) {
                    method = "GET";
                } else {
                    method = method.toUpperCase();
                }

                for (String hrefParam : parseHrefParameters(action)) {
                    // Query parameters are always implied as a GET request, but if the form is a POST request then
                    //  the parameter may be valid for both GET and POST
                    reference.addRequestParameter(hrefParam, "GET", RouteParameterType.QUERY_STRING);
                    if (!method.equals("GET")) {
                        reference.addRequestParameter(hrefParam, method, RouteParameterType.QUERY_STRING);
                    }
                }

                reference.setDefaultRequestType(method);
                reference.setTargetEndpoint(parseEndpointFromQuery(action));
                reference.setDefaultParameterType(RouteParameterType.FORM_DATA);

            } else if (reference.getElementType().equals("a")) {

                String href = reference.getAttributeValue("href");
                if (href == null) {
                    continue;
                }

                for (String hrefParam : parseHrefParameters(href)) {
                    reference.addRequestParameter(hrefParam, "GET", RouteParameterType.QUERY_STRING);
                }

                reference.setDefaultRequestType("GET");
                reference.setTargetEndpoint(parseEndpointFromQuery(href));
                reference.setDefaultParameterType(RouteParameterType.QUERY_STRING);

            } else if (reference.getElementType().equals("textarea")) {

                String name = reference.getAttributeValue("name");
                if (name == null) {
                    continue;
                }

                reference.addRequestParameter(name.trim());
            } else if (reference.getElementType().equals("select")) {

                String name = reference.getAttributeValue("name");
                if (name == null) {
                    continue;
                }

                reference.addRequestParameter(name.trim(), null, RouteParameterType.UNKNOWN, "string");
            } else if (reference.getElementType().equals("option")) {

                String value = reference.getAttributeValue("value");
                if (value == null || value.length() == 0) {
                    continue;
                }

                reference.addRequestParameter(null, null, RouteParameterType.UNKNOWN, null, list(value));

            } else {
                LOG.warn("Got unexpected reference element '<" + reference.getElementType() + ">'");
            }
        }
    }

    private String resolveDataTypeName(String htmlInputType) {
        return null;
    }

    // Returns: map<EndpointPath, map<HttpMethod, List<RouteParameter>>>; List<RouteParameter> may have duplicates
    private Map<String, Map<String, List<RouteParameter>>> parseReferenceParameters(List<ElementReference> referenceTree) {

        Stack<ElementReference> pendingElements = new Stack<ElementReference>();

        // Elements containing accepted parameter values without a parameter name are intended to bubble up
        //  those accepted values to the nearest matching parameter in the elements' parents
        // (Particularly <select> and <option> elements)
        pendingElements.empty();
        pendingElements.addAll(referenceTree);
        while (!pendingElements.isEmpty()) {
            ElementReference element = pendingElements.pop();
            pendingElements.addAll(element.getChildren());
            if (element.getAttributeValue("name") != null || element.getRequestParameters().size() == 0) {
                continue;
            }

            String paramName = null;
            ElementReference parent = element;
            while (parent != null) {
                parent = parent.getParent();

                if (parent == null) {
                    continue;
                }

                if (paramName == null) {
                    if (parent.getAttributeValue("name") != null) {
                        paramName = parent.getAttributeValue("name");
                    } else {
                        continue;
                    }
                }

                HyperlinkSimpleParameter existingParam = parent.getRequestParameter(paramName);
                if (existingParam == null) {
                    continue;
                }

                if (existingParam.acceptedValues == null) {
                    existingParam.acceptedValues = list();
                }

                for (HyperlinkSimpleParameter elementParam : element.getRequestParameters()) {
                    if (elementParam.name == null) {
                        existingParam.acceptedValues.addAll(elementParam.acceptedValues);
                    }
                }
            }
        }

        // Bubble parameters up to their root elements (note - this may incorrectly map parameters if ie an <a> is in a <form>)
        pendingElements.clear();
        pendingElements.addAll(referenceTree);
        while (!pendingElements.isEmpty()) {
            ElementReference element = pendingElements.pop();
            pendingElements.addAll(element.getChildren());
            if (element.getParent() == null) {
                continue;
            }

            // Options need to be processed separately to set them as accepted values for their attached <select> parameter
            if (element.getElementType().equals("option")) {
                continue;
            }

            ElementReference rootElement = element;
            while (rootElement.getParent() != null && rootElement.getDefaultRequestType() == null) {
                rootElement = rootElement.getParent();
            }

            for (HyperlinkSimpleParameter param : element.getRequestParameters()) {

                if (param.name.length() > 0) {
                    String name = param.name;
                    String method = param.httpMethod;
                    RouteParameterType paramType = param.parameterType;
                    List<String> acceptedValues = param.acceptedValues;
                    if (method == null) {
                        if (rootElement.getDefaultRequestType() == null) {
                            LOG.debug("No explicit HTTP method was assigned for parameter '" + name + "' and the parent element does not have a default request method, GET will be assumed");
                            method = "GET";
                        } else {
                            method = rootElement.getDefaultRequestType();
                        }
                    }

                    if (!rootElement.hasParameter(param.name, param.httpMethod)) {
                        if (paramType == RouteParameterType.UNKNOWN) {
                            paramType = rootElement.getDefaultParameterType();
                        }

                        rootElement.addRequestParameter(name, method, paramType, param.inferredDataType, acceptedValues);

                    } else if (param.acceptedValues != null) {
                        HyperlinkSimpleParameter existingParameter = rootElement.getRequestParameter(param.name);
                        if (existingParameter.acceptedValues == null) {
                            existingParameter.acceptedValues = list();
                            for (String value : param.acceptedValues) {
                                if (!existingParameter.acceptedValues.contains(value)) {
                                    existingParameter.acceptedValues.add(value);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, Map<String, List<RouteParameter>>> result = map();
        for (ElementReference root : referenceTree) {
            if (root.getTargetEndpoint() == null || root.getTargetEndpoint().length() == 0) {
                continue;
            }

            String endpoint = root.getTargetEndpoint();
            List<HyperlinkSimpleParameter> params = root.getRequestParameters();

            if (params.isEmpty()) {
                continue;
            }

            if (!result.containsKey(endpoint)) {
                result.put(endpoint, new HashMap<String, List<RouteParameter>>());
            }

            Map<String, List<RouteParameter>> endpointMethodsAndParams = result.get(endpoint);

            for (HyperlinkSimpleParameter param : params) {
                String name = param.name;
                String method = param.httpMethod;
                if (name == null || method == null || name.length() == 0 || method.length() == 0) {
                    continue;
                }

                if (!endpointMethodsAndParams.containsKey(method)) {
                    endpointMethodsAndParams.put(method, new ArrayList<RouteParameter>());
                }

                boolean exists = false;
                List<RouteParameter> currentParams = endpointMethodsAndParams.get(method);
                for (RouteParameter existingParameter : currentParams) {
                    if (existingParameter.getName().equals(name)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    continue;
                }

                RouteParameter newParameter = new RouteParameter(name);
                newParameter.setParamType(param.parameterType);
                currentParams.add(newParameter);
            }
        }

        return result;
    }

    private String parseEndpointFromQuery(String queryString) {
        if (queryString.contains("?")) {
            queryString = queryString.substring(0, queryString.indexOf("?"));
        }
        if (queryString.contains("#")) {
            queryString = queryString.substring(0, queryString.indexOf("#"));
        }
        return queryString;
    }

    private List<String> parseHrefParameters(String href) {
        List<String> result = list();

        if (href.contains("#")) {
            href = href.substring(0, href.indexOf('#'));
        }

        if (href.contains("?")) {
            String[] parts = href.substring(href.indexOf('?') + 1).split("&");
            for (String part : parts) {
                if (part.contains("=")) {
                    result.add(part.substring(0, part.indexOf('=')).trim());
                } else {
                    result.add(part.trim());
                }
            }
        }
        return result;
    }

    private void setOrAddOne(Map<String, Integer> map, String key) {
        if (!map.containsKey(key)) {
            map.put(key, 1);
        } else {
            map.put(key, map.get(key) + 1);
        }
    }

    private int mapMax(Map<String, Integer> map) {
        int max = -1;
        for (Integer entry : map.values()) {
            if (max < entry) {
                max = entry;
            }
        }
        return max;
    }
}
