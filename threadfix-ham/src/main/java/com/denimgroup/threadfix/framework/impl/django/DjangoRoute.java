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
import com.denimgroup.threadfix.framework.util.FilePathUtils;

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
    private String httpMethod;
    private Map<String, RouteParameter> parameters = map();
    private int startLineNumber = -1, endLineNumber = -1;

    public DjangoRoute(String url, String viewPath) {
        this.url = url;
        this.viewPath = FilePathUtils.normalizePath(viewPath);
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public Map<String, RouteParameter> getParameters() {
        return parameters;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void addParameter(String parameter, RouteParameter dataType) {
        parameters.put(parameter, dataType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(DjangoRoute.class)) {
            return false;
        }

        DjangoRoute other = (DjangoRoute)obj;

        return
            this.viewPath.equals(other.viewPath) &&
            this.url.equals(other.url) &&
            this.startLineNumber == other.startLineNumber &&
            this.endLineNumber == other.endLineNumber &&
            (this.httpMethod == null) == (other.httpMethod == null) &&
            (this.httpMethod == null || this.httpMethod.equals(other.httpMethod)) &&
            parametersMatch(this.parameters, other.parameters);
    }

    private static boolean parametersMatch(Map<String, RouteParameter> a, Map<String, RouteParameter> b) {
        if (
            !a.keySet().containsAll(b.keySet()) ||
            !b.keySet().containsAll(a.keySet())
        ) {
            return false;
        }

        for (String paramName : a.keySet()) {
            RouteParameter aParam = a.get(paramName);
            RouteParameter bParam = b.get(paramName);

            if (aParam.getParamType() != bParam.getParamType()) {
                return false;
            } else if (!aParam.getDataType().equals(bParam.getDataType())) {
                return false;
            } else if ((aParam.getAcceptedValues() == null) != (bParam.getAcceptedValues() == null)) {
                return false;
            } else if (aParam.getAcceptedValues() != null) {
                if (
                    !aParam.getAcceptedValues().containsAll(bParam.getAcceptedValues()) ||
                    !bParam.getAcceptedValues().containsAll(aParam.getAcceptedValues())
                ) {
                    return false;
                }
            }
        }

        return true;
    }
}
