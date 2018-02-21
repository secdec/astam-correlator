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

import com.denimgroup.threadfix.data.entities.RouteParameterType;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class ElementReference {
    private String sourceFile;
    private String targetEndpoint;
    private List<ElementReference> children = list();
    private ElementReference parent = null;
    private List<HyperlinkSimpleParameter> namedParameters = list();
    private String defaultRequestType;
    private RouteParameterType defaultParameterType;
    private String originalEndpoint;

    private String elementType;
    private Map<String, String> attributes = map();

    public static List<ElementReference> flattenReferenceTree(List<ElementReference> elements) {
        List<ElementReference> result = list();
        Stack<ElementReference> pendingReferences = new Stack<ElementReference>();
        pendingReferences.addAll(elements);
        while (!pendingReferences.isEmpty()) {
            ElementReference next = pendingReferences.pop();
            result.add(next);
            pendingReferences.addAll(next.getChildren());
        }
        return result;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
        if (this.targetEndpoint != null) {
            this.targetEndpoint = this.targetEndpoint.trim();
        }
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public void setChildren(List<ElementReference> children) {
        this.children = children;
        for (ElementReference child : children) {
            child.parent = this;
        }
    }

    public void addChild(ElementReference child) {
        this.children.add(child);
        child.parent = this;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public void addRequestParameter(String parameterName) {
        this.namedParameters.add(new HyperlinkSimpleParameter(parameterName));
    }

    public void addRequestParameter(String parameterName, String httpMethod) {
        this.namedParameters.add(new HyperlinkSimpleParameter(parameterName, httpMethod));
    }

    public void addRequestParameter(String parameterName, String httpMethod, RouteParameterType parameterType) {
        this.namedParameters.add(new HyperlinkSimpleParameter(parameterName, httpMethod, parameterType));
    }

    public void addRequestParameter(String parameterName, String httpMethod, RouteParameterType parameterType, String dataType) {
        this.namedParameters.add(new HyperlinkSimpleParameter(parameterName, httpMethod, parameterType, dataType));
    }

    public void addRequestParameter(String parameterName, String httpMethod, RouteParameterType parameterType, String dataType, List<String> acceptedValues) {
        HyperlinkSimpleParameter newParam = new HyperlinkSimpleParameter(parameterName, httpMethod, parameterType, dataType);
        newParam.acceptedValues = acceptedValues;
        this.namedParameters.add(newParam);
    }

    public void setDefaultRequestType(String defaultRequestType) {
        this.defaultRequestType = defaultRequestType;
    }

    public void setDefaultParameterType(RouteParameterType defaultParameterType) {
        this.defaultParameterType = defaultParameterType;
    }

    public List<ElementReference> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttributeValue(String name) {
        return attributes.get(name);
    }

    public String getElementType() {
        return elementType;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public List<HyperlinkSimpleParameter> getRequestParameters() {
        return namedParameters;
    }

    public HyperlinkSimpleParameter getRequestParameter(String name) {
        for (HyperlinkSimpleParameter param : namedParameters) {
            if (param.name.equals(name)) {
                return param;
            }
        }
        return null;
    }

    public boolean hasParameter(String name) {
        return hasParameter(name, null);
    }

    public boolean hasParameter(String name, String requestType) {
        for (HyperlinkSimpleParameter param : namedParameters) {
            if (param.name.equals(name) && (requestType == null || param.httpMethod.equalsIgnoreCase(requestType))) {
                return true;
            }
        }
        return false;
    }

    public ElementReference getParent() {
        return parent;
    }

    public String getDefaultRequestType() {
        return defaultRequestType;
    }

    public RouteParameterType getDefaultParameterType() {
        return defaultParameterType;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('<');
        result.append(elementType);
        if (attributes.size() > 0) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                result.append(' ');
                result.append(entry.getKey());
                result.append("=\"");
                result.append(entry.getValue().replaceAll("\"", "'"));
                result.append('"');
            }
        }
        result.append('>');

        for (ElementReference child : getChildren()) {
            result.append(child.toString());
        }

        result.append('<');
        result.append(elementType);
        result.append('>');

        return result.toString();
    }
}
