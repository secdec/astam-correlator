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
