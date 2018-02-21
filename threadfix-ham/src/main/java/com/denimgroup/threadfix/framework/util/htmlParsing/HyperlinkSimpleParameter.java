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
