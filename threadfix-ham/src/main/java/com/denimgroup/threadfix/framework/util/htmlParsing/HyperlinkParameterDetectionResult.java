package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.framework.util.PathInvariantStringMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class HyperlinkParameterDetectionResult {

    private List<ElementReference> sourceReferences;
    private PathInvariantStringMap<Map<String, List<RouteParameter>>> mergedParameters = new PathInvariantStringMap<Map<String, List<RouteParameter>>>();

    protected HyperlinkParameterDetectionResult(@Nonnull List<ElementReference> sourceReferences, @Nonnull Map<String, Map<String, List<RouteParameter>>> parsedParameters) {
        this.sourceReferences = sourceReferences;
        for (Map.Entry<String, Map<String, List<RouteParameter>>> mergedEntry : parsedParameters.entrySet()) {
            this.mergedParameters.put(mergedEntry.getKey(), mergedEntry.getValue());
        }
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
            return endpointRecord.get(httpMethod);
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

    public List<String> getEndpointRequestTypes(String endpoint) {
        Map<String, List<RouteParameter>> endpointRecord = mergedParameters.get(endpoint);
        if (endpointRecord != null) {
            return new ArrayList<String>(endpointRecord.keySet());
        } else {
            return null;
        }
    }

    public List<RouteParameter> getExclusiveParameters(String endpoint, String httpMethod) {
        Map<String, List<RouteParameter>> endpointRecords = mergedParameters.get(endpoint);
        if (endpointRecords == null || endpointRecords.get(httpMethod) == null) {
            return null;
        }

        List<String> commonParameters = list();
        for (Map.Entry<String, List<RouteParameter>> methodEntry : endpointRecords.entrySet()) {
            String currentMethod = methodEntry.getKey();
            if (currentMethod.equalsIgnoreCase(httpMethod)) {
                continue;
            }

            for (RouteParameter param : methodEntry.getValue()) {
                if (!commonParameters.contains(param.getName())) {
                    commonParameters.add(param.getName());
                }
            }
        }

        List<RouteParameter> result = list();
        List<RouteParameter> methodParameters = endpointRecords.get(httpMethod);
        for (RouteParameter param : methodParameters) {
            if (!commonParameters.contains(param.getName())) {
                result.add(param);
            }
        }
        return result;
    }
}
