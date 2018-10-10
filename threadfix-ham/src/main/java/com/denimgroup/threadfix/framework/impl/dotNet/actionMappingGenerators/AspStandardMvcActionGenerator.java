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

package com.denimgroup.threadfix.framework.impl.dotNet.actionMappingGenerators;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerMappings;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetParameterUtil;
import com.denimgroup.threadfix.framework.impl.dotNet.RouteParameterMap;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpParameter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.RESULT_TYPES;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.cleanTypeName;

public class AspStandardMvcActionGenerator implements AspActionGenerator {
    private List<CSharpClass> classes;
    private List<String> classNames;
    private Map<String, RouteParameterMap> routeParameters;

    private static List<String> CONTROLLER_BASE_TYPES = list(
        "Controller",
        "HubController",
        "HubControllerBase",
        "AsyncController",
        "BaseController"
    );

    public AspStandardMvcActionGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;

        this.classNames = list();
        for (CSharpClass cls : classes) {
            this.classNames.add(cls.getName());
        }
    }

    public List<DotNetControllerMappings> generate() {
        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isControllerClass(csClass)) {
                continue;
            }

            DotNetControllerMappings currentMappings = new DotNetControllerMappings(csClass.getFilePath());
            currentMappings.setControllerName(csClass.getName().substring(0, csClass.getName().length() - "Controller".length()));
            currentMappings.setNamespace(csClass.getNamespace());

            CSharpAttribute areaAttribute = csClass.getAttribute("RouteArea");
            if (areaAttribute != null && areaAttribute.getParameterValue(0) != null) {
                currentMappings.setAreaName(areaAttribute.getParameterValue(0).getValue());
            }

            RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
            if (fileParameters == null) {
                fileParameters = new RouteParameterMap();
                routeParameters.put(csClass.getFilePath(), fileParameters);
            }

            for (CSharpMethod method : csClass.getMethods()) {
                List<RouteParameter> methodRouteParameters = fileParameters.findParametersInLines(method.getStartLine(), method.getEndLine());

                addActionFromMethod(currentMappings, method, methodRouteParameters);
            }

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private void addActionFromMethod(DotNetControllerMappings controller, CSharpMethod method, List<RouteParameter> methodRouteParameters) {
        if (method.getAccessLevel() != CSharpMethod.AccessLevel.PUBLIC) {
            return;
        }

        String returnType = cleanTypeName(method.getReturnType());
        if (!RESULT_TYPES.contains(returnType)) {
            return;
        }

        if (method.getAttribute("NonAction") != null) {
            return;
        }

        List<String> attributeNames = list();
        for (CSharpAttribute attribute : method.getAttributes()) {
            attributeNames.add(attribute.getName());
        }

        String explicitPath = null;
        CSharpAttribute routeAttribute = method.getAttribute("Route");
        if (routeAttribute != null) {
            CSharpParameter pathParameter = routeAttribute.getParameterValue("template", 0);
            if (pathParameter == null) {
                pathParameter = routeAttribute.getParameterValue("Name", 0);
            }

            if (pathParameter != null) {
                explicitPath = pathParameter.getValue();
            }
        }

        String actionName = method.getName();
        CSharpAttribute actionNameAttribute = method.getAttribute("ActionName");
        if (actionNameAttribute != null) {
            actionName = actionNameAttribute.getParameterValue("name", 0).getStringValue();
        }

        Collection<RouteParameter> mergedParameters = DotNetParameterUtil.getMergedMethodParameters(method.getParameters(), methodRouteParameters, classNames);

        controller.addAction(
            actionName,
            new HashSet<String>(attributeNames),
            method.getStartLine(),
            method.getEndLine(),
            new HashSet<RouteParameter>(mergedParameters),
            explicitPath,
            method,
            false
        );
    }

    private boolean isControllerClass(CSharpClass csClass) {
        if (!csClass.getName().endsWith("Controller")) {
            return false;
        }

        if (!csClass.getTemplateParameterNames().isEmpty()) {
            return false;
        }

        if (csClass.isAbstract()) {
            return false;
        }

        for (String baseType : csClass.getBaseTypes()) {
            if (CONTROLLER_BASE_TYPES.contains(baseType)) {
                return true;
            }
        }

        return false;
    }
}
