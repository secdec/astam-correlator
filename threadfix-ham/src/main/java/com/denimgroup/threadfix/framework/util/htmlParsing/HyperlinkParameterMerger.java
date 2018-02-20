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

package com.denimgroup.threadfix.framework.util.htmlParsing;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class HyperlinkParameterMerger {

    private boolean addParsedParameters = true;
    private boolean removeUnreferencedParameters = true;
    private boolean mergeOptional = true;

    public HyperlinkParameterMerger(boolean addParsedParameters, boolean removeUnreferencedParameters) {
        this.addParsedParameters = addParsedParameters;
        this.removeUnreferencedParameters = removeUnreferencedParameters;
    }

    public HyperlinkParameterMergingGuide mergeParsedImplicitParameters(List<Endpoint> endpoints, HyperlinkParameterDetectionResult implicitParameters) {
        HyperlinkParameterMergingGuide guide = new HyperlinkParameterMergingGuide();

        for (Endpoint endpoint : endpoints) {
            Map<String, RouteParameter> existingParameters = endpoint.getParameters();
            List<RouteParameter> inferredParameters = implicitParameters.getEndpointParameters(endpoint.getUrlPath(), endpoint.getHttpMethod());
            if (inferredParameters == null) {
                continue;
            }

            //  Update existing parameters and add new ones, if necessary
            for (RouteParameter inferred : inferredParameters) {
                RouteParameter existing = existingParameters.get(inferred.getName());
                if (existing == null) {
                    // There is an inferred parameter that isn't the existing parameter set, add that
                    //  parameter to the merging guide

                    if (addParsedParameters) {
                        Map<String, List<RouteParameter>> paramMapping;
                        if (!guide.addedParameters.containsKey(endpoint.getUrlPath())) {
                             guide.addedParameters.put(endpoint.getUrlPath(), new HashMap<String, List<RouteParameter>>());
                        }
                        paramMapping = guide.addedParameters.get(endpoint.getUrlPath());

                        if (!paramMapping.containsKey(endpoint.getHttpMethod())) {
                            paramMapping.put(endpoint.getHttpMethod(), new ArrayList<RouteParameter>());
                        }
                        List<RouteParameter> newParameters = paramMapping.get(endpoint.getHttpMethod());
                        newParameters.add(inferred);
                    }

                    continue;
                }


                //  Copy 'isOptional', 'parameterType' from the inferred parameters to the existing parameters.


                if (existing.getParamType() == RouteParameterType.UNKNOWN) {
                    existing.setParamType(inferred.getParamType());
                }

                // Copy 'dataType' if the original endpoint isn't assigned one
                if (existing.getDataType() == null) {
                    existing.setDataType(inferred.getDataTypeSource());
                }

                if (inferred.getAcceptedValues() != null && !inferred.getAcceptedValues().isEmpty()) {
                    for (String acceptedValue : inferred.getAcceptedValues()) {
                        existing.addAcceptedValue(acceptedValue);
                    }
                }
            }

            //  Remove parameters from the provided endpoint that weren't used in HTML, according to the parser.
            if (removeUnreferencedParameters) {
                // TODO - If a provided parameter isn't referenced by any HTTP method for an endpoint, that
                //  parameter should be kept as-is across all endpoints as we don't have any information to
                //  base our modifications from.
                List<String> unusedParameters = list();
                for (String existingParamName : existingParameters.keySet()) {
                    boolean isUnused = true;
                    for (RouteParameter inferred : inferredParameters) {
                        if (existingParamName.equalsIgnoreCase(inferred.getName())) {
                            isUnused = false;
                            break;
                        }
                    }
                    if (isUnused) {
                        unusedParameters.add(existingParamName);
                    }
                }

                if (unusedParameters.size() > 0) {
                    Map<String, List<RouteParameter>> removedParameters = guide.removedParameters.get(endpoint.getUrlPath());
                    if (removedParameters == null) {
                        guide.removedParameters.put(endpoint.getUrlPath(), removedParameters = map());
                    }

                    List<RouteParameter> removedMethodParameters = removedParameters.get(endpoint.getHttpMethod());
                    if (removedMethodParameters == null) {
                        removedParameters.put(endpoint.getHttpMethod(), removedMethodParameters = list());
                    }

                    for (String paramName : unusedParameters) {
                        RouteParameter oldParameter = existingParameters.get(paramName);
                        removedMethodParameters.add(oldParameter);
                    }
                }
            }
        }


        //  Detect new HTTP methods from inferred parameters that aren't in the provided endpoints
        for (Endpoint endpoint : endpoints) {
            List<String> discoveredMethods = implicitParameters.getEndpointRequestTypes(endpoint.getUrlPath());
            if (discoveredMethods == null) {
                continue;
            }
            for (String method : discoveredMethods) {
                if (!collectionContainsEndpoint(endpoints, endpoint.getUrlPath(), method)) {
                    List<RouteParameter> discoveredMethodParameters = implicitParameters.getEndpointParameters(endpoint.getUrlPath(), method);

                    Map<String, List<RouteParameter>> methodParametersMap = guide.discoveredHttpMethods.get(endpoint.getUrlPath());
                    if (methodParametersMap == null) {
                        guide.discoveredHttpMethods.put(endpoint.getUrlPath(), methodParametersMap = map());
                    }

                    methodParametersMap.put(method, discoveredMethodParameters);
                }
            }
        }

        //  Detect unreferenced endpoints
        for (Endpoint endpoint : endpoints) {
            List<String> referencedMethods = implicitParameters.getEndpointRequestTypes(endpoint.getUrlPath());
            if (referencedMethods == null || !referencedMethods.contains(endpoint.getHttpMethod())) {
                guide.unreferencedEndpoints.add(endpoint);
            }
        }

        return guide;
    }

    public void setMergeOptional(boolean mergeOptionalIndicators) {
        this.mergeOptional = mergeOptionalIndicators;
    }

    public boolean mergesOptionalParams() {
        return this.mergeOptional;
    }

    private boolean collectionContainsEndpoint(Collection<Endpoint> collection, String endpointPath, String method) {
        for (Endpoint endpoint : collection) {
            if (endpoint.getHttpMethod().equalsIgnoreCase(method) && PathUtil.isEqualInvariant(endpointPath, endpoint.getUrlPath())) {
                return true;
            }
        }
        return false;
    }

}
