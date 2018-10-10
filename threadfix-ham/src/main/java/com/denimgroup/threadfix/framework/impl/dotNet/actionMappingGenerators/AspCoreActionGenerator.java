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
import com.denimgroup.threadfix.framework.impl.dotNet.Action;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetControllerMappings;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetParameterUtil;
import com.denimgroup.threadfix.framework.impl.dotNet.RouteParameterMap;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.*;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

public class AspCoreActionGenerator implements AspActionGenerator {

    private List<CSharpClass> classes;
    private List<String> classNames;
    private Map<String, RouteParameterMap> routeParameters;
    ConventionBasedActionGenerator conventionBasedActionGenerator = new ConventionBasedActionGenerator();

    private static List<String> CONTROLLER_BASE_TYPES = list(
        "Controller",
        "ControllerBase"
    );

    public AspCoreActionGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;

        this.classNames = list();
        for (CSharpClass cls : classes) {
            classNames.add(cls.getName());
        }
    }

    @Override
    public List<DotNetControllerMappings> generate() {
        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isControllerClass(csClass)) {
                continue;
            }

            RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
            if (fileParameters == null) {
                fileParameters = new RouteParameterMap();
                routeParameters.put(csClass.getFilePath(), fileParameters);
            }

            DotNetControllerMappings currentMappings = conventionBasedActionGenerator.generateForClass(csClass, fileParameters, classNames);

            String baseRoute = null;
            CSharpAttribute controllerRouteAttribute = csClass.getAttribute("Route");
            if (controllerRouteAttribute != null) {
                baseRoute = controllerRouteAttribute.getParameterValue("template", 0).getStringValue();
            }

            CSharpAttribute areaAttribute = csClass.getAttribute("Area");
            if (areaAttribute != null) {
                currentMappings.setAreaName(areaAttribute.getParameterValue("areaName", 0).getStringValue());
            }

            for (CSharpMethod method : csClass.getMethods()) {
                if (method.getAccessLevel() != CSharpMethod.AccessLevel.PUBLIC || method.getAttribute("NonAction") != null) {
                    continue;
                }

                //  Method has already been mapped to an action
                Action existingAction = currentMappings.getActionForLines(method.getStartLine(), method.getEndLine());
                if (existingAction != null) {
                    if (baseRoute != null) {
                        //  Explicit base route for this controller is defined and a convention-based action
                        //  has already been generated from this method; change this to an explicitly-routed
                        //  method since route-mapping is unnecessary

                        existingAction.explicitRoute = PathUtil.combine(baseRoute, existingAction.explicitRoute);
                        existingAction.isMethodBasedAction = false;
                    }
                    continue;
                }

                List<CSharpAttribute> httpAttributes = method.getAttributes("HttpGet", "HttpPost", "HttpPut", "HttpPatch", "HttpDelete");
                if (httpAttributes.isEmpty()) {
                    currentMappings.addAction(
                        method.getName(),
                        set("HttpGet"),
                        method.getStartLine(),
                        method.getEndLine(),
                        new HashSet<RouteParameter>(DotNetParameterUtil.getMergedMethodParameters(method, fileParameters, classNames)),
                        null,
                        method,
                        false
                    );
                } else {
                    for (CSharpAttribute attribute : httpAttributes) {
                        String methodPath = null;
                        CSharpParameter methodPathParameter = attribute.getParameterValue("template", 0);
                        if (methodPathParameter != null) {
                            methodPath = methodPathParameter.getStringValue();
                        }

                        String fullMethodPath = null;
                        if (baseRoute != null && methodPath != null) {
                            fullMethodPath = PathUtil.combine(baseRoute, methodPath);
                        }
                        List<RouteParameter> methodRouteParameters = fileParameters.findParametersInLines(method.getStartLine(), method.getEndLine());

                        addActionsFromMethod(currentMappings, fullMethodPath, attribute.getName(), method, methodRouteParameters);
                    }
                }
            }

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private void addActionsFromMethod(DotNetControllerMappings controller, String actionPath, String httpAttributeName, CSharpMethod method, List<RouteParameter> methodRouteParameters) {

        if (method.getAttribute("NonAction") != null) {
            return;
        }

        String actionName = getActionName(method);
        Collection<RouteParameter> mergedParameters = DotNetParameterUtil.getMergedMethodParameters(method.getParameters(), methodRouteParameters, classNames);

        controller.addAction(
            actionName,
            set(httpAttributeName),
            method.getStartLine(),
            method.getEndLine(),
            new HashSet<RouteParameter>(mergedParameters),
            actionPath,
            method,
            false
        );
    }

    private String getActionName(CSharpMethod method) {
        CSharpAttribute actionNameAttribute = method.getAttribute("ActionName");
        if (actionNameAttribute != null) {
            return actionNameAttribute.getParameterValue("name", 0).getStringValue();
        }

        String actionName = method.getName();
        return actionName;
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

        if (csClass.getAttribute("ApiController") != null) {
            return true;
        }

        for (String baseType : csClass.getBaseTypes()) {
            if (CONTROLLER_BASE_TYPES.contains(baseType)) {
                return true;
            }
        }

        return false;
    }
}
