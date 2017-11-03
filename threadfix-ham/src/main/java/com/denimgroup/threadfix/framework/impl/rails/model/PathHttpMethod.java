package com.denimgroup.threadfix.framework.impl.rails.model;

public class PathHttpMethod {

    String path, method, action;

    public PathHttpMethod(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public PathHttpMethod(String path, String method, String action) {
        this.path = path;
        this.method = method;
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getAction() {
        return action;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
