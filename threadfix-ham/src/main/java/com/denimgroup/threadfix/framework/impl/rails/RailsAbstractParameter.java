package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.model.RoutingParameterType;

public class RailsAbstractParameter {

    RoutingParameterType labelType;
    RouteParameterValueType parameterType;
    String name, value;

    public void setLabelType(RoutingParameterType identifierType) {
        this.labelType = identifierType;
    }

    public RoutingParameterType getLabelType() {
        return labelType;
    }

    public void setParameterType(RouteParameterValueType parameterType) {
        this.parameterType = parameterType;
    }

    public RouteParameterValueType getParameterType() {
        return parameterType;
    }

    public void setLabel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
