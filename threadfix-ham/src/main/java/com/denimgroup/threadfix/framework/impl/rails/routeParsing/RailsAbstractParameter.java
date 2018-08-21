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

import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.model.RoutingParameterType;

public class RailsAbstractParameter {

    RoutingParameterType parameterType;
    RouteParameterValueType labelType, valueType;
    String name, value;

    public void setLabelType(RouteParameterValueType labelType) {
        this.labelType = labelType;
    }

    public RouteParameterValueType getLabelType() {
        return labelType;
    }

    public void setParameterType(RoutingParameterType identifierType) {
        this.parameterType = identifierType;
    }

    public RoutingParameterType getParameterType() {
        return parameterType;
    }

    public void setValueType(RouteParameterValueType parameterType) {
        this.valueType = parameterType;
    }

    public RouteParameterValueType getValueType() {
        return valueType;
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
