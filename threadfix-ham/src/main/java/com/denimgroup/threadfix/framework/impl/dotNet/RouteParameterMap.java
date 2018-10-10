////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameterMap extends HashMap<Integer, List<RouteParameter>> {

    public void put(Integer lineNumber, RouteParameter parameter) {
        if (!containsKey(lineNumber)) {
            put(lineNumber, new ArrayList<RouteParameter>());
        }

        get(lineNumber).add(parameter);
    }

    public List<RouteParameter> findParametersInLines(int startLine, int endLine) {
        List<RouteParameter> parameters = list();
        for (Map.Entry<Integer, List<RouteParameter>> entry : entrySet()) {
            int lineNumber = entry.getKey();
            if (lineNumber < startLine || lineNumber > endLine) {
                parameters.addAll(entry.getValue());
            }
        }
        return parameters;
    }
}
