package com.denimgroup.threadfix.framework.impl.jsp;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class JSPElementReference {
    private String sourceFile;
    private String targetFile;
    private List<String> elementParameterNames;
    private List<JSPElementReference> children = list();

    private String elementType;
    private Map<String, String> attributes = map();

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public void setChildren(List<JSPElementReference> children) {
        this.children = children;
    }

    public void addChild(JSPElementReference child) {
        this.children.add(child);
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public void setElementParameterNames(List<String> elementParameterNames) {
        this.elementParameterNames = elementParameterNames;
    }

    public void addElementParameterName(String parameterName) {
        this.elementParameterNames.add(parameterName);
    }

    public List<JSPElementReference> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getElementType() {
        return elementType;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public List<String> getElementParameterNames() {
        return elementParameterNames;
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

        for (JSPElementReference child : getChildren()) {
            result.append(child.toString());
        }

        result.append('<');
        result.append(elementType);
        result.append('>');

        return result.toString();
    }
}
