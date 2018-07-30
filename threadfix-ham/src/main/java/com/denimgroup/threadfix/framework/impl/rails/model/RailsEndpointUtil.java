package com.denimgroup.threadfix.framework.impl.rails.model;

public class RailsEndpointUtil {
    public static String cleanEndpointParameters(String endpoint, RouteParameterValueType parameterNameType) {
        if (parameterNameType == RouteParameterValueType.SYMBOL_STRING_LITERAL) {
            endpoint = ':' + endpoint;
        }
        return endpoint.replaceAll("[:#%]\\{?(\\w+)\\}?", "{$1}");
    }
}
