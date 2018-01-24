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

    public HyperlinkParameterDetector(List<String> watchedElementTypes) {
        watchedElements = new ArrayList<String>(watchedElementTypes);
    }

    private List<String> watchedElements = list("a", "input", "form", "textarea", "select");

    public List<ElementReference> parse(@Nonnull String html, @Nullable File sourceFile) {
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

        return rootElements;
    }

    private void processNode(Node htmlNode, Stack<ElementReference> elementStack, List<ElementReference> rootElements) {
        boolean pushedStack = false;

        String[] quoteTrimTokens = new String[] { "\"", "'" };

        if (watchedElements.contains(htmlNode.nodeName())) {

            ElementReference newElement = new ElementReference();
            newElement.setElementType(htmlNode.nodeName());

            for (Attribute attr : htmlNode.attributes()) {
                newElement.addAttribute(CodeParseUtil.trim(attr.getKey(), quoteTrimTokens, 1), CodeParseUtil.trim(attr.getValue(), quoteTrimTokens, 1));
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

            } else {
                LOG.warn("Got unexpected reference element '<" + reference.getElementType() + ">'");
            }
        }
    }

    // Returns: map<EndpointPath, map<HttpMethod, List<RouteParameter>>>; List<RouteParameter> may have duplicates
    private Map<String, Map<String, List<RouteParameter>>> parseReferenceParameters(List<ElementReference> referenceTree) {
        // Bubble parameters up to their root elements (note - this may incorrectly map parameters if ie an <a> is in a <form>)
        Stack<ElementReference> pendingElements = new Stack<ElementReference>();
        pendingElements.addAll(referenceTree);
        while (!pendingElements.isEmpty()) {
            ElementReference element = pendingElements.pop();
            pendingElements.addAll(element.getChildren());
            if (element.getParent() == null) {
                continue;
            }

            ElementReference rootElement = element;
            while (rootElement.getParent() != null && rootElement.getDefaultRequestType() == null) {
                rootElement = rootElement.getParent();
            }

            for (HyperlinkSimpleParameter param : element.getRequestParameters()) {
                if (param.name.length() > 0 && !rootElement.hasParameter(param.name, param.httpMethod)) {
                    String name = param.name;
                    String method = param.httpMethod;
                    RouteParameterType paramType = param.parameterType;
                    if (method == null) {
                        if (rootElement.getDefaultRequestType() == null) {
                            LOG.warn("No explicit HTTP method was assigned for parameter '" + name + "' and the parent element does not have a default request method, GET will be assumed");
                            method = "GET";
                        } else {
                            method = rootElement.getDefaultRequestType();
                        }
                    }

                    if (paramType == RouteParameterType.UNKNOWN) {
                        paramType = rootElement.getDefaultParameterType();
                    }

                    rootElement.addRequestParameter(name, method, paramType);
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

                List<RouteParameter> currentParams = endpointMethodsAndParams.get(method);
                RouteParameter newParameter = new RouteParameter();
                newParameter.setName(name);
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
}
