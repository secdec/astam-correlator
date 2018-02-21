////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

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
        int numValidParamTypes = 0;
        int numValidDataTypes = 0;
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
                if (param.getParamType() != RouteParameterType.UNKNOWN) {
                    numValidParamTypes++;
                }
                if (param.getDataType() != null) {
                    numValidDataTypes++;
                }
            }
        }

        LOG.info("Endpoint stats: " +
            numValidParamTypes + "/" + numParams + " params have valid parameter types, " +
            numValidDataTypes + "/" + numParams + " params have valid data types, " +
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
