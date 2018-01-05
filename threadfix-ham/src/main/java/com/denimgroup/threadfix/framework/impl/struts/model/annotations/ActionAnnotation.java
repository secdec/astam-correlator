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

//  See: https://struts.apache.org/docs/convention-plugin.html#ConventionPlugin-Actionannotation

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ActionAnnotation extends Annotation {
    String boundUrl;
    List<ResultAnnotation> results = list();
    Map<String, String> params = new HashMap<String, String>();
    String explicitClassName;



    public String getBoundUrl() {
        return boundUrl;
    }

    public Collection<ResultAnnotation> getResults() {
        return results;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getExplicitClassName() {
        return explicitClassName;
    }




    public void setBoundUrl(String url) {
        boundUrl = url;
    }

    public void addParameter(String name, String value) {
        params.put(name, value);
    }

    public void addResult(ResultAnnotation result) {
        results.add(result);
    }

    public void setExplicitClassName(String className) {
        explicitClassName = className;
    }
}
