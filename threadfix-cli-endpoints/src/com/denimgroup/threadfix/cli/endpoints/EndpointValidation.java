package com.denimgroup.threadfix.cli.endpoints;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointSerialization;
import com.denimgroup.threadfix.framework.util.EndpointUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EndpointValidation {
    public static boolean validateSerialization(FrameworkType framework, List<Endpoint> endpoints) {
        List<Endpoint> allEndpoints = EndpointUtil.flattenWithVariants(endpoints);
        for (Endpoint endpoint : allEndpoints) {
            String serialized;
            Endpoint deserialized;
            try {
                serialized = EndpointSerialization.serialize(framework, endpoint);
            } catch (IOException e) {
                System.out.println("Exception occurred while serializing: " + endpoint.toString());
                e.printStackTrace();
                return false;
            }

            try {
                deserialized = EndpointSerialization.deserialize(framework, serialized);
            } catch (IOException e) {
                System.out.println("Exception occurred while deserializing: " + endpoint.toString());
                e.printStackTrace();
                return false;
            }

            if (!deserialized.getUrlPath().equals(endpoint.getUrlPath())) {
                System.out.println("Failed to validate serialization due to mismatched URL paths on " + endpoint.toString());
                return false;
            }

            if (!deserialized.getFilePath().equals(endpoint.getFilePath())) {
                System.out.println("Failed to validate serialization due to mismatched FILE paths on " + endpoint.toString());
                return false;
            }

            if (deserialized.getParameters().size() != endpoint.getParameters().size()) {
                System.out.println("Failed to validate serialization due to mismatched PARAMETER COUNTS on " + endpoint.toString());
                return false;
            }

            if (!deserialized.getHttpMethod().equals(endpoint.getHttpMethod())) {
                System.out.println("Failed to validate serialization due to mismatched HTTP METHOD on " + endpoint.toString());
                return false;
            }

            Map<String, RouteParameter> endpointParams = endpoint.getParameters();
            Map<String, RouteParameter> deserializedParams = deserialized.getParameters();

            if (!endpointParams.keySet().containsAll(deserializedParams.keySet()) ||
                    !deserializedParams.keySet().containsAll(endpointParams.keySet())) {

                System.out.println("Failed to validate serialization due to mismatched PARAMETER NAMES on " + endpoint.toString());
                return false;
            }

            for (String param : endpointParams.keySet()) {
                RouteParameter endpointParam = endpointParams.get(param);
                RouteParameter deserializedParam = deserializedParams.get(param);

                if (endpointParam.getParamType() != deserializedParam.getParamType()) {
                    System.out.println("Failed to validate serialization due to mismatched PARAM TYPE on " + endpoint.toString());
                    return false;
                }

                if ((endpointParam.getDataTypeSource() == null) != (deserializedParam.getDataTypeSource() == null)) {
                    System.out.println("Failed to validate serialization due to mismatched PARAM DATA TYPE on " + endpoint.toString());
                    return false;
                }

                if (endpointParam.getDataTypeSource() != null && !endpointParam.getDataTypeSource().equals(deserializedParam.getDataTypeSource())) {
                    System.out.println("Failed to validate serialization due to mismatched PARAM DATA TYPE on " + endpoint.toString());
                    return false;
                }

                if (!endpointParam.getName().equals(deserializedParam.getName())) {
                    System.out.println("Failed to validate serialization due to mismatched PARAM NAME on " + endpoint.toString());
                    return false;
                }

                if ((endpointParam.getAcceptedValues() == null) != (deserializedParam.getAcceptedValues() == null)) {
                    System.out.println("Failed to validate serialization due to mismatched ACCEPTED PARAM VALUES on " + endpoint.toString());
                    return false;
                } else if (endpointParam.getAcceptedValues() != null) {
                    if (!endpointParam.getAcceptedValues().containsAll(deserializedParam.getAcceptedValues()) ||
                            !deserializedParam.getAcceptedValues().containsAll(endpointParam.getAcceptedValues())) {
                        System.out.println("Failed to validate serialization due to mismatched ACCEPTED PARAM VALUES on " + endpoint.toString());
                        return false;
                    }
                }
            }

        }
        return true;
    }
}
