package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

import javax.annotation.Nonnull;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameter {
    ParameterDataType dataType;
    String dataTypeSource;
    boolean isOptional = false;
    RouteParameterType paramType = RouteParameterType.UNKNOWN;
    String name;
    List<String> acceptedValues = null;

    public RouteParameter(String name) {
        this.name = name;
    }

    public static RouteParameter fromDataType(String name, String dataType) {
        RouteParameter result = new RouteParameter(name);
        result.setDataType(dataType);
        return result;
    }

    public static RouteParameter fromDataType(String name, ParameterDataType dataType) {
        RouteParameter result = new RouteParameter(name);
        result.setDataType(dataType.getDisplayName());
        return result;
    }

    public RouteParameterType getParamType() {
        return paramType;
    }

    public ParameterDataType getDataType() {
        return dataType;
    }

    public boolean hasProperDataType() {
        return dataType != null && dataType.getDisplayName().toLowerCase().equalsIgnoreCase(dataTypeSource);
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

    // Use 'setDataType(String)' instead; the data type is automatically parsed and the original type string
    // will be maintained for later type references lookups.
    @Deprecated
    public void setDataType(ParameterDataType dataType) {
        this.dataType = dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = ParameterDataType.getType(dataType);
        this.dataTypeSource = dataType;
    }

    public String getDataTypeSource() {
        return this.dataTypeSource;
    }

    public void setParamType(RouteParameterType paramType) {
        this.paramType = paramType;
    }

    public void setName(@Nonnull String name) {
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
