package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameter {
    ParameterDataType dataType;
    boolean isOptional = false;
    RouteParameterType paramType = RouteParameterType.UNKNOWN;
    String name;
    List<String> acceptedValues = null;


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

    public String getName() {
        return name;
    }

    public List<String> getAcceptedValues() {
        return acceptedValues;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setAcceptedValues(List<String> acceptedValues) {
        this.acceptedValues = acceptedValues;
    }

    public void addAcceptedValue(String value) {
        if (this.acceptedValues == null) {
            this.acceptedValues = list();
        }
        if (!this.acceptedValues.contains(value)) {
            this.acceptedValues.add(value);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("name=");
        result.append(name);
        result.append(", paramType=");
        result.append(paramType);
        result.append(", dataType=");
        result.append(dataType);
        result.append(", isOptional=");
        result.append(isOptional);

        return result.toString();
    }
}
