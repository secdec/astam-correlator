package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import sun.misc.Request;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class HyperlinkParameterDetectionResult {

    private List<ElementReference> sourceReferences;
    private Map<String, Map<String, List<RouteParameter>>> mergedParameters;

    protected HyperlinkParameterDetectionResult(@Nonnull List<ElementReference> sourceReferences, @Nonnull Map<String, Map<String, List<RouteParameter>>> mergedParameters) {
        this.sourceReferences = sourceReferences;
        this.mergedParameters = mergedParameters;
    }

    public List<ElementReference> getRootSourceReferences() {
        return new ArrayList<ElementReference>(sourceReferences);
    }

    public List<ElementReference> getAllSourceReferences() {
        return ElementReference.flattenReferenceTree(sourceReferences);
    }

    public Set<String> getEndpoints() {
        return mergedParameters.keySet();
    }

    public Map<String, List<RouteParameter>> getEndpointParameters(String endpoint) {
        return mergedParameters.get(endpoint);
    }

    public List<RouteParameter> getEndpointParameters(String endpoint, String httpMethod) {
        Map<String, List<RouteParameter>> endpointRecord = mergedParameters.get(endpoint);
        if (endpointRecord != null) {
            return endpointRecord.get(httpMethod.toUpperCase());
        } else {
            return null;
        }
    }

    public List<RouteParameter> getEndpointParameters(String endpoint, String httpMethod, RouteParameterType paramType) {
        List<RouteParameter> params = getEndpointParameters(endpoint, httpMethod);
        if (params != null) {
            List<RouteParameter> result = list();
            for (RouteParameter param : params) {
                if (param.getParamType() == paramType) {
                    result.add(param);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    public List<RouteParameter> getEndpointParameters(String endpoint, RouteParameterType paramType) {
        Map<String, List<RouteParameter>> endpointRecord = mergedParameters.get(endpoint);
        List<RouteParameter> result = list();
        if (endpointRecord != null) {
            for (Map.Entry<String, List<RouteParameter>> entry : endpointRecord.entrySet()) {
                for (RouteParameter param : entry.getValue()) {
                    if (param.getParamType() == paramType) {
                        result.add(param);
                    }
                }
            }
        }
        return result;
    }

    public Set<String> getEndpointRequestTypes(String endpoint) {
        Map<String, List<RouteParameter>> endpointRecord = mergedParameters.get(endpoint);
        if (endpointRecord != null) {
            return endpointRecord.keySet();
        } else {
            return null;
        }
    }
}
