////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.ModelFieldSet;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.util.EndpointUtil;
import com.denimgroup.threadfix.framework.util.EndpointValidationStatistics;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.ParameterMerger;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.dotNet.DotNetPathCleaner.cleanStringFromCode;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetEndpointGenerator implements EndpointGenerator {

    private final List<DotNetControllerMappings> dotNetControllerMappings;
    private final DotNetRouteMappings            dotNetRouteMappings;
    private final DotNetModelMappings            dotNetModelMappings;
    private final List<Endpoint> endpoints = list();

    public static final SanitizedLogger LOG = new SanitizedLogger(DotNetEndpointGenerator.class);

    public DotNetEndpointGenerator(File rootDirectory,
                                   DotNetRouteMappings routeMappings,
                                   DotNetModelMappings modelMappings,
                                   DotNetControllerMappings... controllerMappings) {
        this(rootDirectory, routeMappings, modelMappings, Arrays.asList(controllerMappings));
    }

    public DotNetEndpointGenerator(File rootDirectory,
                                   DotNetRouteMappings routeMappings,
                                   DotNetModelMappings modelMappings,
                                   List<DotNetControllerMappings> controllerMappings) {
        assert routeMappings != null : "routeMappings was null. Check route parsing code.";
        assert controllerMappings != null : "controllerMappings was null. Check controller parsing code.";
        assert controllerMappings.size() != 0 : "controllerMappings were empty. Check controller parsing code.";

        LOG.debug("Initializing EndpointGenerator with routeMappings: " + routeMappings + " and controllerMappings: " + controllerMappings);

        dotNetControllerMappings = controllerMappings;
        dotNetRouteMappings = routeMappings;
        dotNetModelMappings = modelMappings;

        assembleEndpoints(rootDirectory);
        expandAmbiguousEndpoints();

        ParameterMerger merger = new ParameterMerger();
        Map<Endpoint, Map<String, RouteParameter>> allMergedParameters = merger.mergeParametersIn(endpoints);

        for (Map.Entry<Endpoint, Map<String, RouteParameter>> endpointMapEntry : allMergedParameters.entrySet()) {
            DotNetEndpoint endpoint = (DotNetEndpoint)endpointMapEntry.getKey();
            Map<String, RouteParameter> mergedEndpointParameters = endpointMapEntry.getValue();
            Map<String, RouteParameter> currentParameters = endpoint.getParameters();

            for (Map.Entry<String, RouteParameter> mergedParameter : mergedEndpointParameters.entrySet()) {
                String paramName = mergedParameter.getKey();
                //  Known parametric endpoints take precedence over any merging recommendations
	            if (currentParameters.get(paramName).getParamType() == RouteParameterType.PARAMETRIC_ENDPOINT) {
	            	continue;
	            }

                RouteParameter mergedParam = mergedParameter.getValue();
                currentParameters.put(paramName, mergedParam);
            }
        }

        EndpointUtil.rectifyVariantHierarchy(endpoints);

        EndpointValidationStatistics.printValidationStats(endpoints);
    }

    private void assembleEndpoints(File rootDirectory) {
        if (dotNetRouteMappings == null || dotNetRouteMappings.routes == null) {
            LOG.error("No mappings found for project. Exiting.");
            return; // can't do anything without routes
        }

        List<DotNetRouteMappings.MapRoute> visitedRoutes = list();

        for (DotNetControllerMappings mappings : dotNetControllerMappings) {
            if (mappings.getControllerName() == null) {
                LOG.debug("Controller Name was null. Skipping to the next.");
                assert false;
                continue;
            }

            DotNetRouteMappings.MapRoute mapRoute = dotNetRouteMappings.getMatchingMapRoute(mappings.hasAreaName(), mappings.getControllerName());

            if (mapRoute == null ||  mapRoute.url == null || mapRoute.url.equals(""))
                continue;

            if (!visitedRoutes.contains(mapRoute)) {
            	visitedRoutes.add(mapRoute);
            }

            for (Action action : mappings.getActions()) {
                if (action == null) {
                    LOG.debug("Action was null. Skipping to the next.");
                    assert false : "mappings.getActions() returned null. This shouldn't happen.";
                    continue;
                }

                String pattern = mapRoute.url;

                LOG.debug("Substituting patterns from route " + action + " into template " + pattern);

                String result = pattern
                        // substitute in controller name for {controller}
                        .replaceAll("\\{\\w*controller\\w*\\}", mappings.getControllerName());
                if(mappings.hasAreaName()){
                    result = result.replaceAll("\\{\\w*area\\w*\\}", mappings.getAreaName());
                }

                if (action.name.equals("Index")) {
                    result = result.replaceAll("/\\{\\w*action\\w*\\}", "");
                } else {
                    result = result.replaceAll("\\{\\w*action\\w*\\}", action.name);
                }

                boolean shouldReplaceParameterSection = true;

                if(action.parameters != null &&
                        mapRoute.defaultRoute != null &&
                        action.parameters.keySet().contains(mapRoute.defaultRoute.parameter)) {

                    String lowerCaseParameterName = mapRoute.defaultRoute.parameter.toLowerCase();
                    for (String parameter : action.parameters.keySet()) {
                        if (parameter.toLowerCase().equals(lowerCaseParameterName)) {
                            shouldReplaceParameterSection = false;
                            break;
                        }
                    }
                }

                if (shouldReplaceParameterSection) {
                    result = result.replaceAll("/\\{[^\\}]*\\}", "");
                }

                // Commented since this would remove valuable information regarding parametric routes
                //result = cleanStringFromCode(result);

                if (!result.startsWith("/")) {
                    result = "/" + result;
                }

                expandParameters(action);

                LOG.debug("Got result " + result);

                String filePath = mappings.getFilePath();
                if (filePath.startsWith(rootDirectory.getAbsolutePath())) {
                    filePath = FilePathUtils.getRelativePath(filePath, rootDirectory);
                }
                endpoints.add(new DotNetEndpoint(result, filePath, action));
            }
        }

        //  Add routes that only have default controllers specified (which wouldn't have been
	    //  enumerated in the previous loop)
        List<DotNetRouteMappings.MapRoute> unvisitedRoutes = new ArrayList<DotNetRouteMappings.MapRoute>(dotNetRouteMappings.routes);
        unvisitedRoutes.removeAll(visitedRoutes);
        for (DotNetRouteMappings.MapRoute route : unvisitedRoutes) {
        	if (route.defaultRoute == null) {
        		continue;
	        }

	        DotNetRouteMappings.ConcreteRoute defaultRoute = route.defaultRoute;
        	String result = route.url;
        	if (!result.startsWith("/")) {
        		result = "/" + result;
	        }

	        DotNetControllerMappings controllerMappings = null;
        	Action action = null;
        	for (DotNetControllerMappings mappings : dotNetControllerMappings) {
        		if (controllerMappings != null) {
        			break;
		        }

        		if (mappings.getControllerName() != null && mappings.getControllerName().equals(defaultRoute.controller)) {
        			for (Action controllerAction : mappings.getActions()) {
        				if (controllerAction.name.equals(defaultRoute.action)) {
					        controllerMappings = mappings;
					        action = controllerAction;
					        break;
				        }
			        }
		        }
	        }

	        if (controllerMappings == null || action == null) {
        		continue;
	        }

	        String filePath = controllerMappings.getFilePath();
        	if (filePath.startsWith(rootDirectory.getAbsolutePath())) {
        		filePath = FilePathUtils.getRelativePath(filePath, rootDirectory);
	        }

	        endpoints.add(new DotNetEndpoint(result, filePath, action));
        }

    }

    private void expandParameters(Action action) {
        if (dotNetModelMappings != null) {

            for (RouteParameter param : action.parametersWithTypes) {
                if (param.getDataTypeSource() == null) {
                    continue;
                }

                ModelFieldSet parameters = dotNetModelMappings.getPossibleParametersForModelType(param.getDataTypeSource());
                if (!parameters.getFieldSet().isEmpty()) {
                    action.parameters.remove(param.getName());
                    for (ModelField possibleParameter : parameters) {
                        RouteParameter newParam = new RouteParameter(possibleParameter.getParameterKey());
                        newParam.setDataType(possibleParameter.getType());
                        newParam.setParamType(RouteParameterType.FORM_DATA); // All non-primitives are serialized as form data
                        action.parameters.put(possibleParameter.getParameterKey(), newParam);
                    }
                }
            }
        }
    }

    private void expandAmbiguousEndpoints() {
        List<DotNetEndpoint> ambiguousEndpoints = list();
        List<DotNetEndpoint> dedicatedEndpoints = list();
        for (Endpoint endpoint : endpoints) {
            DotNetEndpoint dotEndpoint = (DotNetEndpoint)endpoint;
            if (dotEndpoint.hasMultipleMethods()) {
                ambiguousEndpoints.add(dotEndpoint);
                List<DotNetEndpoint> splitEndpoints = dotEndpoint.splitByMethods();
                DotNetEndpoint primaryEndpoint = splitEndpoints.get(0);
                for (DotNetEndpoint subEndpoint : splitEndpoints) {
                    if (subEndpoint != primaryEndpoint) {
                        primaryEndpoint.addVariant(subEndpoint);
                    }
                }
                dedicatedEndpoints.add(primaryEndpoint);
            }
        }

        endpoints.removeAll(ambiguousEndpoints);
        endpoints.addAll(dedicatedEndpoints);
    }

    // TODO consider making this read-only with Collections.unmodifiableList() or returning a defensive copy
    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return endpoints;
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return endpoints.iterator();
    }
}
