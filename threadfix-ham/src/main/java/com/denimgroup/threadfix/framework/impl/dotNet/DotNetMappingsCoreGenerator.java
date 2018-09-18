package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.*;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetKeywords.RESULT_TYPES;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetSyntaxUtil.cleanTypeName;

public class DotNetMappingsCoreGenerator implements DotNetMappingsGenerator {

    private List<CSharpClass> classes;
    private Map<String, RouteParameterMap> routeParameters;

    private static List<String> CONTROLLER_BASE_TYPES = list(
        "Controller"
    );

    public DotNetMappingsCoreGenerator(List<CSharpClass> classes, Map<String, RouteParameterMap> routeParameters) {
        this.classes = classes;
        this.routeParameters = routeParameters;
    }

    @Override
    public List<DotNetControllerMappings> generate() {
        List<DotNetControllerMappings> controllerMappings = list();

        for (CSharpClass csClass : classes) {
            if (!isControllerClass(csClass)) {
                continue;
            }

            DotNetControllerMappings currentMappings = new DotNetControllerMappings(csClass.getFilePath());
            currentMappings.setControllerName(csClass.getName().substring(0, csClass.getName().length() - "Controller".length()));
            currentMappings.setNamespace(csClass.getName());

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
                if (method.getAccessLevel() != CSharpMethod.AccessLevel.PUBLIC) {
                    continue;
                }

                RouteParameterMap fileParameters = routeParameters.get(csClass.getFilePath());
                if (fileParameters == null) {
                    fileParameters = new RouteParameterMap();
                    routeParameters.put(csClass.getFilePath(), fileParameters);
                }

                List<CSharpAttribute> httpAttributes = method.getAttributes("HttpGet", "HttpPost", "HttpPut", "HttpPatch", "HttpDelete");
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

            controllerMappings.add(currentMappings);
        }

        return controllerMappings;
    }

    private void addActionsFromMethod(DotNetControllerMappings controller, String actionPath, String httpAttributeName, CSharpMethod method, List<RouteParameter> methodRouteParameters) {

        String actionName = getActionName(method);
        Collection<RouteParameter> mergedParameters = DotNetParameterUtil.getMergedMethodParameters(method.getParameters(), methodRouteParameters);

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

        for (String baseType : csClass.getBaseTypes()) {
            if (CONTROLLER_BASE_TYPES.contains(baseType)) {
                return true;
            }
        }

        return false;
    }
}
