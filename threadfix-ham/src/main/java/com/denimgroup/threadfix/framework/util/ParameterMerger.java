package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class ParameterMerger {

    boolean isCaseSensitive = false;
    boolean mergeOptional = true;

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        isCaseSensitive = caseSensitive;
    }

    public boolean mergeOptional() {
        return this.mergeOptional;
    }

    public void setMergeOptional(boolean mergeOptional) {
        this.mergeOptional = mergeOptional;
    }

    public Map<String, RouteParameter> merge(Collection<RouteParameter> parameters) {
        // 1. Generate canonical name map
        // ------ If case-insensitive, collect all references with the same name and use the one with most
        // ------ UpperCase chars as the canonical name
        // Map: paramName -> CanonicalName
        Map<String, String> canonicalNameMap = map();

        if (isCaseSensitive) {
            for (RouteParameter param : parameters) {
                canonicalNameMap.put(param.getName(), param.getName());
            }
        } else {
            Map<String, List<String>> commonNameMap = map();
            for (RouteParameter param : parameters) {
                String paramName = param.getName();
                String lowerName = paramName.toLowerCase();

                List<String> recordedNames = commonNameMap.get(lowerName);
                if (recordedNames == null) {
                    commonNameMap.put(lowerName, list(paramName));
                } else {
                    if (!recordedNames.contains(paramName)) {
                        recordedNames.add(paramName);
                    }
                }
            }

            for (Map.Entry<String, List<String>> entry : commonNameMap.entrySet()) {
                List<String> discoveredNames = entry.getValue();
                String bestName = getMostCapitalizedString(discoveredNames);

                for (String name : discoveredNames) {
                    canonicalNameMap.put(name, bestName);
                }
            }
        }



        // 2. Collect parameters with the same name
        Map<String, List<RouteParameter>> groupedParameters = map();
        for (RouteParameter param : parameters) {
            String paramName = param.getName();
            String paramCanonicalName = canonicalNameMap.get(paramName);

            List<RouteParameter> existingGroup = groupedParameters.get(paramCanonicalName);
            if (existingGroup == null) {
                groupedParameters.put(paramCanonicalName, list(param));
            } else {
                existingGroup.add(param);
            }
        }

        // Merge parameters with the same name
        Map<String, RouteParameter> mergedParameters = map();
        for (Map.Entry<String, List<RouteParameter>> entry : groupedParameters.entrySet()) {
            if (entry.getValue().size() <= 1) {
                continue;
            }

            String name = entry.getKey();

            Map<String, Integer> parameterDataTypeSourceFrequency = map();
            Map<RouteParameterType, Integer> parameterTypeFrequency = map();
            Map<Boolean, Integer> optionalFrequency = map();
            List<String> longestAcceptedParametersList = null;

            for (RouteParameter param : entry.getValue()) {
                String typeSource = param.getDataTypeSource();
                RouteParameterType parameterType = param.getParamType();

                setOneOrIncrement(parameterDataTypeSourceFrequency, typeSource);
                setOneOrIncrement(parameterTypeFrequency, parameterType);

                if (param.getAcceptedValues() != null && param.getAcceptedValues().size() > 0) {
                    if (longestAcceptedParametersList == null) {
                        longestAcceptedParametersList = param.getAcceptedValues();
                    } else if (param.getAcceptedValues().size() > longestAcceptedParametersList.size()) {
                        longestAcceptedParametersList = param.getAcceptedValues();
                    }
                }
            }

            String mostCommonDataTypeSource = highestFrequencyEntry(parameterDataTypeSourceFrequency);
            RouteParameterType mostCommonParameterType = highestFrequencyEntry(parameterTypeFrequency);

            RouteParameter consolidatedParameter = new RouteParameter(name);
            consolidatedParameter.setParamType(mostCommonParameterType);
            consolidatedParameter.setDataType(mostCommonDataTypeSource);
            consolidatedParameter.setAcceptedValues(longestAcceptedParametersList);

            mergedParameters.put(name, consolidatedParameter);
        }

        //  De-canonicalize names
        if (!isCaseSensitive) {
            for (Map.Entry<String, String> entry : canonicalNameMap.entrySet()) {
                String originalName = entry.getKey();
                String canonicalName = entry.getValue();

                if (originalName.equalsIgnoreCase(canonicalName)) {
                    continue;
                }

                RouteParameter mergedParameter = mergedParameters.get(canonicalName);
                mergedParameters.put(originalName, mergedParameter);
            }
        }

        return mergedParameters;
    }

    public Map<Endpoint, Map<String, RouteParameter>> mergeParametersIn(Collection<Endpoint> endpoints) {
        // 1. Collect all references to each parameter across all endpoints
        // 2. Run `merge` across this parameter collection
        // 3. For each parameter, find all endpoints referencing that parameter by name and add it to
        // ------ the result mapping

        List<RouteParameter> allParameters = list();
        for (Endpoint endpoint : endpoints) {
            allParameters.addAll(endpoint.getParameters().values());
        }

        Map<String, RouteParameter> mergedParameters = merge(allParameters);
        Map<Endpoint, Map<String, RouteParameter>> result = map();

        for (Endpoint endpoint : endpoints) {
            for (String paramName : endpoint.getParameters().keySet()) {
                if (!mergedParameters.containsKey(paramName)) {
                    continue;
                }

                RouteParameter mergedParam = mergedParameters.get(paramName);
                Map<String, RouteParameter> remappedParams = result.get(endpoint);
                if (remappedParams == null) {
                    result.put(endpoint, remappedParams = map());
                }
                remappedParams.put(paramName, mergedParam);
            }
        }

        return result;
    }

    private void updateSimilarKeys(Map<String, String> map, String baseKey, String newValue) {
        List<String> similarKeys = getSimilarKeys(map, baseKey);

        for (String key : similarKeys) {
            map.put(key, newValue);
        }
    }

    private List<String> getSimilarKeys(Map<String, String> map, String baseKey) {
        List<String> similarKeys = list();
        for (String key : map.keySet()) {
            if (key.equalsIgnoreCase(baseKey)) {
                similarKeys.add(key);
            }
        }
        return similarKeys;
    }

    private String getMostCapitalizedString(List<String> strings) {
        int maxCaps = -1;
        String bestVersion = null;
        for (String string : strings) {
            int numCaps = 0;
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (c != Character.toLowerCase(c)) {
                    numCaps++;
                }
            }
            if (maxCaps < numCaps) {
                bestVersion = string;
            }
        }
        return bestVersion;
    }

    private <K> void setOneOrIncrement(Map<K, Integer> map, K key) {
        if (!map.containsKey(key)) {
            map.put(key, 1);
        } else {
            map.put(key, map.get(key) + 1);
        }
    }

    private <K> K highestFrequencyEntry(Map<K, Integer> frequencyMap) {
        int largest = -1;
        K result = null;
        for (Map.Entry<K, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > largest) {
                result = entry.getKey();
            }
        }
        return result;
    }

}
