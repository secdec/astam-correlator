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

import com.denimgroup.threadfix.framework.impl.dotNet.*;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpAttribute;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.setFrom;

public class AspStandardApiActionGenerator implements AspActionGenerator {

    List<CSharpClass> classes;
    List<String> classNames;
    Map<String, RouteParameterMap> routeParameters;
    ConventionBasedActionGenerator conventionBasedActionGenerator = new ConventionBasedActionGenerator();

    public AspStandardApiActionGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;

        this.classNames = list();
        for (CSharpClass cls : classes) {
            this.classNames.add(cls.getName());
        }
    }

    @Override
    public List<DotNetControllerMappings> generate() {

        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isApiControllerClass(csClass)) {
                continue;
            }

            RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
            if (fileParameters == null) {
                fileParameters = new RouteParameterMap();
                routeParameters.put(csClass.getFilePath(), fileParameters);
            }

            DotNetControllerMappings currentMappings = conventionBasedActionGenerator.generateForClass(csClass, fileParameters, classNames);
            generateExplicitRoutes(currentMappings, csClass, fileParameters);

            CSharpAttribute areaAttribute = csClass.getAttribute("RouteArea");
            if (areaAttribute != null && areaAttribute.getParameterValue(0) != null) {
                currentMappings.setAreaName(areaAttribute.getParameterValue(0).getValue());
            }

            CSharpAttribute routePrefixAttribute = csClass.getAttribute("RoutePrefix");
            if (routePrefixAttribute != null) {
                String prefix = routePrefixAttribute.getParameterValue("prefix", 0).getStringValue();
                //  Update actions with prefix
                for (Action action : currentMappings.getActions()) {
                    if (action.isMethodBasedAction) {
                        action.explicitRoute = PathUtil.combine(prefix, action.explicitRoute);
                        //  It's still a "method-based action" but we don't need to process it as
                        //  such during endpoint generation (otherwise DotNetEndpointGenerator
                        //  tries to use the explicit route as relative to the 'MapRoute' associated
                        //  with the controller)
                        action.isMethodBasedAction = false;
                    } else {
                        String actionPath = action.explicitRoute;
                        if (actionPath == null) {
                            actionPath = action.name;
                        }

                        if (actionPath.startsWith("~")) {
                            //  Route definition overrides route prefix
                            action.explicitRoute = actionPath.substring(1);
                        } else {
                            action.explicitRoute = PathUtil.combine(prefix, actionPath);
                        }
                    }
                }
            }

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private void generateExplicitRoutes(DotNetControllerMappings mappings, CSharpClass csClass, RouteParameterMap fileParameters) {
        ConventionBasedActionGenerator convention = new ConventionBasedActionGenerator();

        for (CSharpMethod method : csClass.getMethods(CSharpMethod.AccessLevel.PUBLIC)) {
            CSharpAttribute routeAttribute = method.getAttribute("Route");
            if (routeAttribute == null) {
                continue;
            }

            List<CSharpAttribute> httpMethodAttributes = DotNetAttributeUtil.findHttpAttributes(method);
            if (httpMethodAttributes.isEmpty()) {
                httpMethodAttributes.add(new CSharpAttribute(convention.detectHttpAttributeByName(method.getName())));
            }

            String path = routeAttribute.getParameterValue("template", 0).getStringValue();
            List<String> httpAttributeNames = list();
            for (CSharpAttribute httpAttribute : httpMethodAttributes) {
                httpAttributeNames.add(httpAttribute.getName());
            }

            mappings.addAction(
                method.getName(),
                setFrom(httpAttributeNames),
                method.getStartLine(),
                method.getEndLine(),
                setFrom(DotNetParameterUtil.getMergedMethodParameters(method, fileParameters, classNames)),
                path,
                method,
                false
            );
        }
    }

    private boolean isApiControllerClass(CSharpClass csClass) {
        return csClass.getName().endsWith("Controller") && csClass.getBaseTypes().contains("ApiController") && !csClass.isAbstract();
    }
}
