package com.denimgroup.threadfix.framework.impl.jsp;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class JSPElementReference {
    String sourceFile;
    String targetFile;
    List<JSPElementReference> children = list();

    String elementType;
    Map<String, String> attributes = map();

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
}
