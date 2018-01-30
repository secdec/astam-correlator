package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameterType;

import java.util.List;

public class HyperlinkSimpleParameter {

    public HyperlinkSimpleParameter() {
        this(null, null, RouteParameterType.UNKNOWN, null);
    }

    public HyperlinkSimpleParameter(String name) {
        this(name, null, RouteParameterType.UNKNOWN, null);
    }

    public HyperlinkSimpleParameter(String name, String httpMethod) {
        this(name, httpMethod, RouteParameterType.UNKNOWN, null);
    }

    public HyperlinkSimpleParameter(String name, String httpMethod, RouteParameterType parameterType) {
        this(name, httpMethod, parameterType, null);
    }

    public HyperlinkSimpleParameter(String name, String httpMethod, RouteParameterType parameterType, String inferredDataType) {
        this.name = name;
        this.httpMethod = httpMethod;
        this.parameterType = parameterType;
        this.inferredDataType = inferredDataType;
    }

    public String name;
    public String httpMethod;
    public RouteParameterType parameterType;
    public String inferredDataType;
    public List<String> acceptedValues;
}
