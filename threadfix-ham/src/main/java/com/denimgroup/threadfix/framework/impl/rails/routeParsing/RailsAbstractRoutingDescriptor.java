package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsAbstractRoutingDescriptor {

    List<RailsAbstractParameter> parameters = list();

    String identifier;
    int lineNumber = -1;

    RailsAbstractRoutingDescriptor parentDescriptor;
    List<RailsAbstractRoutingDescriptor> childDescriptors = list();

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public List<RailsAbstractParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RailsAbstractParameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(RailsAbstractParameter parameter) {
        this.parameters.add(parameter);
    }

    public void setParentDescriptor(RailsAbstractRoutingDescriptor parentDescriptor) {
        this.parentDescriptor = parentDescriptor;

        if (!parentDescriptor.childDescriptors.contains(this)) {
            parentDescriptor.childDescriptors.add(this);
        }
    }

    public RailsAbstractRoutingDescriptor getParentDescriptor() {
        return parentDescriptor;
    }

    public List<RailsAbstractRoutingDescriptor> getChildDescriptors() {
        return childDescriptors;
    }

    public void setChildDescriptors(List<RailsAbstractRoutingDescriptor> childDescriptors) {
        this.childDescriptors = childDescriptors;
    }

    public void addChildDescriptor(RailsAbstractRoutingDescriptor descriptor) {
        this.childDescriptors.add(descriptor);
        descriptor.parentDescriptor = this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(identifier);
        result.append(' ');

        for (RailsAbstractParameter param : parameters) {
            result.append(' ');
            if (param.getName() != null) {
                result.append(param.getName());
                result.append(" (");
                result.append(param.getLabelType());
                result.append(") ");
            }

            result.append(param.getValue());
            result.append(" (");
            result.append(param.getParameterType());
            result.append(")");
        }

        return result.toString();
    }
}
