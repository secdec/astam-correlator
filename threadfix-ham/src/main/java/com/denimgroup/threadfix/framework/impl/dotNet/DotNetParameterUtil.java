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
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class DotNetParameterUtil {
    private static String cleanUrlParameterName(String name) {
        return name.replace("*", "");
    }

    public static Collection<RouteParameter> getMergedMethodParameters(List<CSharpParameter> cSharpParameters, List<RouteParameter> routeParameters, List<String> modelTypeNames) {
        Map<String, RouteParameter> parameterMap = map();

        for (RouteParameter existingParam : routeParameters) {
            parameterMap.put(cleanUrlParameterName(existingParam.getName()), existingParam);
        }

        for (CSharpParameter csParam : cSharpParameters) {
            RouteParameter existingParam = parameterMap.get(csParam.getName());
            if (existingParam == null) {
                existingParam = new RouteParameter(csParam.getName());
                parameterMap.put(csParam.getName(), existingParam);
            }

            //  Use specified attributes if available
            if (csParam.getAttribute("FromBody") != null || csParam.getAttribute("FromForm") != null) {
                existingParam.setParamType(RouteParameterType.FORM_DATA);
            } else if (csParam.getAttribute("FromQuery") != null) {
                existingParam.setParamType(RouteParameterType.QUERY_STRING);
            } else if (csParam.getAttribute("FromFile") != null) {
                existingParam.setParamType(RouteParameterType.FILES);
            } else if (csParam.getAttribute("FromRoute") != null) {
                existingParam.setParamType(RouteParameterType.PARAMETRIC_ENDPOINT);
            } else if (csParam.getAttribute("FromServices") != null) {
                parameterMap.remove(csParam.getName());
                continue;
            } else if (existingParam.getParamType() == RouteParameterType.UNKNOWN) {
                // No attributes specified, make best-guess
                if (csParam.getType().contains("IFormFile")) {
                    existingParam.setParamType(RouteParameterType.FORM_DATA);
                } else if (modelTypeNames.contains(DotNetSyntaxUtil.cleanTypeName(csParam.getType()))) {
                    existingParam.setParamType(RouteParameterType.FORM_DATA);
                } else {
                    existingParam.setParamType(RouteParameterType.QUERY_STRING);
                }
            }

            existingParam.setDataType(csParam.getType());
        }

        return parameterMap.values();
    }

    public static Collection<RouteParameter> getMergedMethodParameters(CSharpMethod method, RouteParameterMap parameterMap, List<String> modelTypeNames) {
        return getMergedMethodParameters(method.getParameters(), parameterMap.findParametersInLines(method.getStartLine(), method.getEndLine()), modelTypeNames);
    }
}
