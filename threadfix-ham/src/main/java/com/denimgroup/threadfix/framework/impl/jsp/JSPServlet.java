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

package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.data.entities.RouteParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class JSPServlet {
    private String packageName;
    private String className;
    private String filePath;
    private List<String> annotatedEndpoints = list();

    private Map<String, List<RouteParameter>> methodParameters = map();
    private Map<Integer, List<RouteParameter>> parameterLineMap = map();

    public JSPServlet(String packageName, String className, String filePath) {
        this.packageName = packageName;
        this.className = className;
        this.filePath = filePath;
    }

    public JSPServlet(String packageName, String className, String filePath, Map<Integer, List<RouteParameter>> parameterLineMap) {
        this(packageName, className, filePath);
        if (parameterLineMap != null) {
            this.parameterLineMap = parameterLineMap;
        }
    }



    public void addEndpoint(String endpoint) {
        annotatedEndpoints.add(endpoint);
    }

    public void addParameter(String httpMethod, RouteParameter parameter) {

        httpMethod = httpMethod.toUpperCase();

        List<RouteParameter> currentParams = methodParameters.get(httpMethod);
        if (currentParams == null) {
            methodParameters.put(httpMethod, currentParams = list());
        }
        currentParams.add(parameter);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getAbsoluteName() {
        return packageName + "." + className;
    }

    public List<String> getAnnotatedEndpoints() {
        return annotatedEndpoints;
    }

    public Map<String, List<RouteParameter>> getAllMethodParameters() {
        return methodParameters;
    }

    public Set<String> getHttpMethods() {
        return methodParameters.keySet();
    }

    public List<RouteParameter> getMethodParameters(String httpMethod) {
        return methodParameters.get(httpMethod.toUpperCase());
    }

    public Map<Integer, List<String>> getParameterLineMap() {
        Map<Integer, List<String>> simpleParameterMap = map();
        for (int line : parameterLineMap.keySet()) {
            List<String> lineParameterNames = list();
            for (RouteParameter param : parameterLineMap.get(line)) {
                lineParameterNames.add(param.getName());
            }
            simpleParameterMap.put(line, lineParameterNames);
        }
        return simpleParameterMap;
    }
}
