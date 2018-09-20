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

    public static Collection<RouteParameter> getMergedMethodParameters(List<CSharpParameter> cSharpParameters, List<RouteParameter> routeParameters) {
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

            if (csParam.getAttribute("FromBody") != null) {
                existingParam.setParamType(RouteParameterType.FORM_DATA);
            }

            existingParam.setDataType(csParam.getType());
        }

        return parameterMap.values();
    }

    public static Collection<RouteParameter> getMergedMethodParameters(CSharpMethod method, RouteParameterMap parameterMap) {
        return getMergedMethodParameters(method.getParameters(), parameterMap.findParametersInLines(method.getStartLine(), method.getEndLine()));
    }
}
