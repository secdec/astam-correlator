package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class EndpointValidationStatistics {

    private final static SanitizedLogger LOG = new SanitizedLogger(EndpointValidationStatistics.class);

    public static void printValidationStats(Collection<Endpoint> endpoints) {

        Map<String, Integer> occurrences = map();
        int numOptionalParams = 0;
        int numValidParamTypes = 0;
        int numParams = 0;
        int numDuplicates = 0;

        for (Endpoint endpoint : endpoints) {
            String uniqueEndpointString = endpoint.getUrlPath() + "-" + endpoint.getHttpMethod();
            if (occurrences.containsKey(uniqueEndpointString)) {
                int currentCnt = occurrences.get(uniqueEndpointString);
                if (currentCnt == 1) {
                    numDuplicates++;
                }
                occurrences.put(uniqueEndpointString, 1 + currentCnt);
            } else {
                occurrences.put(uniqueEndpointString, 1);
            }

            for (RouteParameter param : endpoint.getParameters().values()) {
                ++numParams;
                if (param.isOptional()) {
                    numOptionalParams++;
                }
                if (param.getParamType() != RouteParameterType.UNKNOWN) {
                    numValidParamTypes++;
                }
            }
        }

        LOG.info("Endpoint stats: " +
            numOptionalParams + "/" + numParams + " params are optional, " +
            numValidParamTypes + "/" + numParams + " params have valid types, " +
            numDuplicates + "/" + endpoints.size() + " duplicate endpoints"
            );

        if (numDuplicates > 0) {
            LOG.info("Duplicate endpoints:");
            List<String> printedEndpoints = list();
            for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
                if (entry.getValue() > 1 && !printedEndpoints.contains(entry.getKey())) {
                    LOG.info("- " + entry.getKey() + " (" + entry.getValue() + ")");
                }
            }
        }

    }

}
