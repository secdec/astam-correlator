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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPServlet {
    private String packageName;
    private String className;
    private String filePath;
    private List<String> annotatedEndpointBindings = list();

    private Map<Integer, List<String>> parameters = new HashMap<Integer, List<String>>();

    public JSPServlet(String packageName, String className, String filePath) {
        this.packageName = packageName;
        this.className = className;
        this.filePath = filePath;
    }

    public JSPServlet(String packageName, String className, String filePath, Map<Integer, List<String>> parameters) {
        this(packageName, className, filePath);
        if (parameters != null) {
            this.parameters = parameters;
        }
    }


    public void addParameter(int lineNumber, String parameterName) {
        if (!parameters.containsKey(lineNumber)) {
            parameters.put(lineNumber, new ArrayList<String>());
        }

        List<String> knownParameters = parameters.get(lineNumber);
        knownParameters.add(parameterName);
    }


    public void addAnnotationBinding(String endpoint) {
        annotatedEndpointBindings.add(endpoint);
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

    public List<String> getAnnotatedEndpointBindings() {
        return annotatedEndpointBindings;
    }

    public Map<Integer, List<String>> getParameters() {
        return parameters;
    }
}
