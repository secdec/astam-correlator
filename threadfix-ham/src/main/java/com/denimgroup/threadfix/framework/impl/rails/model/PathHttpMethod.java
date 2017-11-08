package com.denimgroup.threadfix.framework.impl.rails.model;

import javax.annotation.Nonnull;

public class PathHttpMethod {

    String path, method, action, controllerName;

//    public PathHttpMethod(@Nonnull String path, @Nonnull String method) {
//        this.path = path;
//        this.method = method;
//    }
//
//    public PathHttpMethod(@Nonnull String path, String method, @Nonnull String action) {
//        this.path = path;
//        this.method = method;
//        this.action = action;
//    }

    public PathHttpMethod(String path, String method, String action, String controllerName) {
        this.path = path;
        this.method = method;
        this.action = action;
        this.controllerName = controllerName;
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

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getControllerName() {
        return controllerName;
    }

    @Override
    public String toString() {
        return this.path + " (" + method + ") => '" + controllerName + "#" + action + "'";
    }
}
