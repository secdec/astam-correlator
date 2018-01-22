package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

public class RouteParameter {
    ParameterDataType dataType;
    boolean isOptional = false;
    RouteParameterType paramType = RouteParameterType.UNKNOWN;


    public static RouteParameter fromDataType(ParameterDataType dataType) {
        RouteParameter result = new RouteParameter();
        result.setDataType(dataType);
        result.setOptional(false);
        result.setParamType(RouteParameterType.UNKNOWN);
        return result;
    }

    public RouteParameterType getParamType() {
        return paramType;
    }

    public ParameterDataType getDataType() {
        return dataType;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public void setDataType(ParameterDataType dataType) {
        this.dataType = dataType;
    }

    public void setParamType(RouteParameterType paramType) {
        this.paramType = paramType;
    }
}
