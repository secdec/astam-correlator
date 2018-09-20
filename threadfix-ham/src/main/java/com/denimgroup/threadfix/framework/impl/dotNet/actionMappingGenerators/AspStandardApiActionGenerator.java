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
    Map<String, RouteParameterMap> routeParameters;
    ConventionBasedActionGenerator conventionBasedActionGenerator = new ConventionBasedActionGenerator();

    public AspStandardApiActionGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;
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

            DotNetControllerMappings currentMappings = conventionBasedActionGenerator.generateForClass(csClass, fileParameters);
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
                setFrom(DotNetParameterUtil.getMergedMethodParameters(method, fileParameters)),
                path,
                method,
                false
            );
        }
    }

    private boolean isApiControllerClass(CSharpClass csClass) {
        return csClass.getName().endsWith("Controller") && csClass.getBaseTypes().contains("ApiController");
    }
}
