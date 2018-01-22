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

package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsWebPack;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.actionEndpointEnumerators.ActionEndpointEnumerator;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;
import static com.denimgroup.threadfix.CollectionUtils.set;

//  Resolves struts.xml route/action bindings and forwards unbound queries to direct files in the _webapp_ folder

public class DefaultActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String namespace) {

        String allActionExtensions = project.getConfig().get("struts.action.extension", "action,");

        String[] actionExtensions = allActionExtensions.split(",", -1);
        StrutsWebPack primaryWebPack = project.findWebPack("/");

        List<StrutsEndpoint> endpoints = list();
        //  Handle basic content files
        for (String file : primaryWebPack.getRelativeFilePaths()) {
            String fullPath = PathUtil.combine(primaryWebPack.getRootDirectoryPath(), file);
            Map<String, RouteParameter> params = new HashMap<String, RouteParameter>();
            StrutsEndpoint endpoint = new StrutsEndpoint(makeRelativePath(fullPath, project), PathUtil.combine(namespace, file), list("GET"), params);
            endpoints.add(endpoint);
        }

        for (String file : primaryWebPack.getWelcomeFiles()) {
            String fullPath = PathUtil.combine(primaryWebPack.getRootDirectoryPath(), file);
            Map<String, RouteParameter> params = new HashMap<String, RouteParameter>();
            StrutsEndpoint endpoint;
            endpoint = new StrutsEndpoint(makeRelativePath(fullPath, project), namespace, list("GET"), params);
            endpoints.add(endpoint);
            endpoint = new StrutsEndpoint(makeRelativePath(fullPath, project), namespace + "/", list("GET"), params);
            endpoints.add(endpoint);
        }

        //  Handle struts-mapped actions
        for (StrutsPackage strutsPackage : packages) {
            String packageNamespace = strutsPackage.getNamespace();
            if (packageNamespace == null)
                packageNamespace = "/";

            packageNamespace = PathUtil.combine(namespace, packageNamespace);

            if(strutsPackage.getActions() == null) {
                continue;
            }

            for (StrutsAction strutsAction : strutsPackage.getActions()) {

                StringBuilder sbUrl = new StringBuilder(packageNamespace);
                String actionName = strutsAction.getName();

                if (!packageNamespace.contentEquals("/") || !packageNamespace.endsWith("/")) {
                    sbUrl.append("/");
                }

                if (actionName.startsWith("/")) {
                    actionName = actionName.substring(1);
                }

                sbUrl.append(actionName);

                if (strutsAction.getActClass() == null) {
                    Collection<StrutsResult> results = strutsAction.getResults();
                    if (results == null) {
                        continue;
                    }
                    for (StrutsResult result : results) {
                        if (primaryWebPack.contains(result.getValue())) {
                            strutsAction.setActClass("JSPServlet");
                            strutsAction.setActClassLocation(PathUtil.combine(primaryWebPack.getRootDirectoryPath(), result.getValue()));
                            break;
                        }
                    }
                    if (strutsAction.getActClass() == null) {
                        continue;
                    }
                }

                String classLocation = strutsAction.getActClassLocation();
                StrutsClass classForAction = project.findClassByFileLocation(classLocation);
                Set<ModelField> fieldMappings = set();
                if (classForAction != null) {
                    fieldMappings = classForAction.getProperties();
                }
                List<String> httpMethods = list();
                Map<String, RouteParameter> parameters = map();

                String basePath = sbUrl.toString();

                ActionEndpointEnumerator endpointEnumerator = new ActionEndpointEnumerator();
                Collection<String> availablePaths = endpointEnumerator.getPossibleEndpoints(basePath, actionExtensions);

                for (String path : availablePaths) {
                    if (path.contains("*") && classForAction != null) { // wildcard
                        for (StrutsMethod method : classForAction.getMethods()) {
                            path = sbUrl.toString();
                            httpMethods = list();
                            parameters = map();
                            if ("execute".equals(method.getName())) {
                                path = path.replace("!*", "");
                                path = path.replace("*", "");
                                httpMethods.add("GET");
                                endpoints.add(new StrutsEndpoint(makeRelativePath(classLocation, project), path, httpMethods, parameters));
                            } else {
                                path = path.replace("*", method.getName());
                                httpMethods.add("POST");
                                for (ModelField mf : fieldMappings) {
                                    parameters.put(mf.getParameterKey(), RouteParameter.fromDataType(ParameterDataType.getType(mf.getType())));
                                }
                                endpoints.add(new StrutsEndpoint(makeRelativePath(classLocation, project), path, httpMethods, parameters));
                            }
                        }
                    } else {
                        httpMethods.add("POST");
                        for (ModelField mf : fieldMappings) {
                            parameters.put(mf.getParameterKey(), RouteParameter.fromDataType(ParameterDataType.getType(mf.getType())));
                        }
                        endpoints.add(new StrutsEndpoint(makeRelativePath(classLocation, project), path, httpMethods, parameters));
                    }
                }

                //  Map actions to their results if they don't specify a target method/class on their own
                Collection<StrutsResult> results = strutsAction.getResults();
                if (results != null && classForAction == null) {
                    for (StrutsResult result : results) {
                        String filePath = result.getValue();

                        if (filePath != null) {
                            if (primaryWebPack.contains(filePath)) {
                                String exposedContentPath = PathUtil.combine(basePath, actionName);
                                endpoints.add(new StrutsEndpoint(makeRelativePath(classLocation, project), exposedContentPath, list("GET"), new HashMap<String, RouteParameter>()));
                            }
                        }
                    }
                }
            }
        }

        return endpoints;
    }

    String makeRelativePath(String path, StrutsProject project) {
        if (project.getRootDirectory() != null && path.startsWith(project.getRootDirectory())) {
            return FilePathUtils.getRelativePath(path, project.getRootDirectory());
        } else {
            return path;
        }
    }

}
