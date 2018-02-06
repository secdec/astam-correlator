package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.util.PathInvariantStringMap;

import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  We don't have access to modify the data in an Endpoint; instead, provide
//  a guide as to which parameters to add
public class ParameterMergingGuide {

    PathInvariantStringMap<Map<String, List<RouteParameter>>> addedParameters = new PathInvariantStringMap<Map<String, List<RouteParameter>>>();
    PathInvariantStringMap<Map<String, List<RouteParameter>>> removedParameters = new PathInvariantStringMap<Map<String, List<RouteParameter>>>();
    PathInvariantStringMap<Map<String, List<RouteParameter>>> discoveredHttpMethods = new PathInvariantStringMap<Map<String, List<RouteParameter>>>();
    List<Endpoint> unreferencedEndpoints = list();

    public List<RouteParameter> findAddedParameters(Endpoint endpoint, String method) {
        Map<String, List<RouteParameter>> addedEndpointParamsMap = addedParameters.get(endpoint.getUrlPath());
        if (addedEndpointParamsMap == null) {
            return null;
        }

        return addedEndpointParamsMap.get(method);
    }

    public List<RouteParameter> findRemovedParameters(Endpoint endpoint, String method) {
        Map<String, List<RouteParameter>> removedEndpointParamsMap = removedParameters.get(endpoint.getUrlPath());
        if (removedEndpointParamsMap == null) {
            return null;
        }

        return removedEndpointParamsMap.get(method);
    }

    public Map<String, List<RouteParameter>> findDiscoveredHttpMethods(Endpoint forEndpoint) {
        return discoveredHttpMethods.get(forEndpoint.getUrlPath());
    }

    public List<Endpoint> getUnreferencedEndpoints(Endpoint forEndpoint) {
        return unreferencedEndpoints;
    }

    public boolean hasData() {
        return  addedParameters.size() > 0 ||
                removedParameters.size() > 0 ||
                discoveredHttpMethods.size() > 0 ||
                unreferencedEndpoints.size() > 0;
    }

}

