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

package com.denimgroup.threadfix.framework.impl.struts.model.annotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  See:    https://struts.apache.org/docs/result-annotation.html
//          https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Resultannotation


public class ResultAnnotation extends Annotation {

    String resultName = "success";
    String location; // named "location" or "value" as param name
    String type; // "NullResult", "PlainTextResult", etc.

    Map<String, String> params = new HashMap<String, String>();

    public void setResultName(String name) {
        resultName = name;
    }

    public void setResultLocation(String location) {
        this.location = location;
    }

    public void setResultType(String type) {
        this.type = type;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }

    public String getResultName() {
        return resultName;
    }

    public String getResultLocation() {
        return location;
    }

    public String getResultType() {
        return type;
    }

    public Map<String, String> getParameters() {
        return params;
    }


    @Override
    public boolean equals(Object obj) {
        if (!ResultAnnotation.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        ResultAnnotation other = (ResultAnnotation)obj;
        if ((this.resultName == null) != (other.resultName == null)) {
            return false;
        }

        if (this.codeLine != other.codeLine) {
            return false;
        }

        if (this.params.size() != other.params.size()) {
            return false;
        }

        for (Map.Entry<String, String> param : params.entrySet()) {
            if (!other.params.containsKey(param.getKey())) {
                return false;
            }

            String otherVal = other.params.get(param.getKey());
            if (!otherVal.equals(param.getValue())) {
                return false;
            }
        }

        return true;
    }
}
