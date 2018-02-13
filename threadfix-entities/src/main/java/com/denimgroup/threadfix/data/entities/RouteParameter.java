package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

import javax.annotation.Nonnull;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameter {
    private ParameterDataType dataType;
    private String dataTypeSource;
    private RouteParameterType paramType = RouteParameterType.UNKNOWN;
    private String name;
    private List<String> acceptedValues = null;

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

    public boolean isArrayType() {
        return dataTypeSource.contains("[]");
    }

    public String getName() {
        return name;
    }

    public List<String> getAcceptedValues() {
        return acceptedValues;
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

        if (acceptedValues != null) {
            result.append(", acceptedValues=[");
            for (int i = 0; i < acceptedValues.size(); i++) {
                if (i > 0) {
                    result.append(',');
                }
                result.append(acceptedValues.get(i));
            }
            result.append(']');
        }

        return result.toString();
    }
}
