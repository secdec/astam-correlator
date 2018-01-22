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
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by csotomayor on 6/13/2017.
 */
public class DjangoRoute {
    private String url;
    private String viewPath;
    private List<String> httpMethods = list();
    private Map<String, RouteParameter> parameters = map();
    private int startLineNumber = -1, endLineNumber = -1;

    public DjangoRoute(String url, String viewPath) {
        this.url = url;
        this.viewPath = viewPath;
    }

    public void setLineNumbers(int startLine, int endLine) {
        startLineNumber = startLine;
        endLineNumber = endLine;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public String getUrl() {
        return url;
    }

    public String getViewPath() {
        return viewPath;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    public void addHttpMethod(String httpMethod) {
        httpMethods.add(httpMethod);
    }

    public void addParameter(String parameter, RouteParameter dataType) {
        parameters.put(parameter, dataType);
    }
}
