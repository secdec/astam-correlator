////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsAbstractRouteEntryDescriptor {

    List<RailsAbstractParameter> parameters = list();
    List<RailsAbstractParameter> initializerParameters = list();

    String identifier;
    int lineNumber = -1;

    RailsAbstractRouteEntryDescriptor parentDescriptor;
    List<RailsAbstractRouteEntryDescriptor> childDescriptors = list();

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

    public List<RailsAbstractParameter> getInitializerParameters() {
        return initializerParameters;
    }

    public void setInitializerParameters(List<RailsAbstractParameter> initializerParameters) {
        this.initializerParameters = initializerParameters;
    }

    public void addInitializerParameter(RailsAbstractParameter initializerParameter) {
        this.initializerParameters.add(initializerParameter);
    }

    public void setParentDescriptor(RailsAbstractRouteEntryDescriptor parentDescriptor) {
        this.parentDescriptor = parentDescriptor;

        if (!parentDescriptor.childDescriptors.contains(this)) {
            parentDescriptor.childDescriptors.add(this);
        }
    }

    public RailsAbstractRouteEntryDescriptor getParentDescriptor() {
        return parentDescriptor;
    }

    public List<RailsAbstractRouteEntryDescriptor> getChildDescriptors() {
        return childDescriptors;
    }

    public void setChildDescriptors(List<RailsAbstractRouteEntryDescriptor> childDescriptors) {
        this.childDescriptors = childDescriptors;
    }

    public void addChildDescriptor(RailsAbstractRouteEntryDescriptor descriptor) {
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
