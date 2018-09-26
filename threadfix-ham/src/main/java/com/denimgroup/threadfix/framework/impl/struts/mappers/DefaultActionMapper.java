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
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsWebPack;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.actionEndpointEnumerators.ActionEndpointEnumerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
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

        if (primaryWebPack != null) {
            //  Handle basic content files
            for (String file : primaryWebPack.getRelativeFilePaths()) {
                String fullPath = PathUtil.combine(primaryWebPack.getRootDirectoryPath(), file);
                Map<String, RouteParameter> params = new HashMap<String, RouteParameter>();
                String endpointPath = makeRelativePath(fullPath, project);
                StrutsEndpoint endpoint = new StrutsEndpoint(endpointPath, PathUtil.combine(namespace, file), "GET", params);
                endpoint.setDisplayFilePath(fullPath);

                int numLines = CodeParseUtil.countLines(fullPath);
                if (numLines > 0) {
                	endpoint.setLineNumbers(1, numLines);
                }
                endpoints.add(endpoint);
            }

            for (String file : primaryWebPack.getWelcomeFiles()) {
                String fullPath = PathUtil.combine(primaryWebPack.getRootDirectoryPath(), file);
                Map<String, RouteParameter> params = new HashMap<String, RouteParameter>();
                String endpointPath = makeRelativePath(fullPath, project);
                StrutsEndpoint endpoint;

                if (!new File(fullPath).exists()) {
                    continue;
                }

                int numLines = CodeParseUtil.countLines(fullPath);

                endpoint = new StrutsEndpoint(endpointPath, namespace, "GET", params);
                endpoint.setDisplayFilePath(fullPath);
                if (numLines > 0) endpoint.setLineNumbers(1, numLines);
                endpoints.add(endpoint);


                endpoint = new StrutsEndpoint(endpointPath, namespace + "/", "GET", params);
                endpoint.setDisplayFilePath(fullPath);
                if (numLines > 0) endpoint.setLineNumbers(1, numLines);
                endpoints.add(endpoint);
            }
        }

        //  Handle struts-mapped actions
        for (StrutsPackage strutsPackage : packages) {
            String packageNamespace = strutsPackage.getNamespace();
            if (packageNamespace == null)
                packageNamespace = "/";

            packageNamespace = PathUtil.combine(namespace, packageNamespace);

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
                    if (primaryWebPack != null) {
                        for (StrutsResult result : results) {
                            if (primaryWebPack.contains(result.getValue())) {
                                strutsAction.setActClass("JSPServlet");
                                strutsAction.setActClassLocation(PathUtil.combine(primaryWebPack.getRootDirectoryPath(), result.getValue()));
                                break;
                            }
                        }
                    }
                    if (strutsAction.getActClass() == null) {
                        continue;
                    }
                }

                String classLocation = strutsAction.getActClassLocation();
                if (classLocation != null)
                    classLocation = FilePathUtils.normalizePath(classLocation);

                StrutsClass classForAction = project.getCodebase().findClassByFileLocation(classLocation);
                Set<ModelField> fieldMappings = set();
                if (classForAction != null) {
                    if (!classForAction.getBaseTypes().contains("ActionSupport") && !classForAction.getName().equals("JSPServlet")) {
                        continue;
                    }
                }
                Map<String, RouteParameter> parameters = map();

                String basePath = sbUrl.toString();

                ActionEndpointEnumerator endpointEnumerator = new ActionEndpointEnumerator();
                Collection<String> availablePaths = endpointEnumerator.getPossibleEndpoints(basePath, actionExtensions);

                for (String path : availablePaths) {
                    if (path.contains("*") && classForAction != null) { // wildcard
                        for (StrutsMethod method : classForAction.getMethods()) {

                            //  Response methods must return strings
                            if (!method.getReturnType().equals("String")) {
                                continue;
                            }

                            //  Ignore getters/setters
                            if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
                                continue;
                            }

                            if (strutsAction.getAllowedMethodNames().size() > 0 && !strutsAction.getAllowedMethodNames().contains(method.getName())) {
                                continue;
                            }

                            String methodPath = path;
                            parameters = map();

                            if (strutsAction.getMethod() != null && strutsAction.getMethod().startsWith("{")) {
                                String wildcardIndexText = strutsAction.getMethod().substring(1, strutsAction.getMethod().length() - 1);
                                int index;
                                try {
                                    index = Integer.parseInt(wildcardIndexText);
                                } catch (NumberFormatException ex) {
                                    index = -1;
                                }
                                if (index < 0) {
                                    continue;
                                }

                                int wildcardStartIndex = StringUtils.ordinalIndexOf(methodPath, "*", index);
                                String firstPart = methodPath.substring(0, wildcardStartIndex);
                                String secondPart = methodPath.substring(wildcardStartIndex + 1);
                                methodPath = firstPart + method.getName() + secondPart;
                            }

                            for (ModelField modelField : classForAction.getProperties()) {
                                RouteParameter newParameter = new RouteParameter(modelField.getParameterKey());
                                newParameter.setParamType(RouteParameterType.QUERY_STRING);
                                newParameter.setDataType(modelField.getType());
                                parameters.put(modelField.getParameterKey(), newParameter);
                            }

                            String httpMethod;
                            if (method.getName().equals("execute")) {
                                httpMethod = "GET";
                            } else {
                                httpMethod = "POST";
                            }
                            StrutsEndpoint newEndpoint = new StrutsEndpoint(makeRelativePath(classLocation, project), methodPath, httpMethod, parameters);
                            newEndpoint.setLineNumbers(method.getStartLine(), method.getEndLine());
                            if (strutsAction.getPrimaryResult() != null) {
                                newEndpoint.setDisplayFilePath(strutsAction.getPrimaryResult().getValue());
                            }
                            endpoints.add(newEndpoint);
                        }
                    } else {
                        StrutsMethod executeMethod = null;
                        if (classForAction != null) {
                            executeMethod = classForAction.getMethod(strutsAction.getMethod());
                        }
                        for (ModelField mf : fieldMappings) {
                            RouteParameter asParam = RouteParameter.fromDataType(mf.getParameterKey(), mf.getType());
                            asParam.setParamType(RouteParameterType.FORM_DATA);
                            parameters.put(mf.getParameterKey(), asParam);
                        }
                        StrutsEndpoint newEndpoint = new StrutsEndpoint(makeRelativePath(classLocation, project), path, "GET", parameters);

                        if (executeMethod != null) {
                            newEndpoint.setLineNumbers(executeMethod.getStartLine(), executeMethod.getEndLine());
                        }
                        StrutsResult primaryResult = strutsAction.getPrimaryResult();
                        if (primaryResult != null) {
                            newEndpoint.setDisplayFilePath(PathUtil.combine(project.getWebPath(), primaryResult.getValue()));
                        }
                        if (newEndpoint.getDisplayFilePath() != null && newEndpoint.getStartingLineNumber() < 0 && newEndpoint.getEndingLineNumber() < 0) {
                        	int lineCount = CodeParseUtil.countLines(newEndpoint.getDisplayFilePath());
                        	if (lineCount > 0) {
                        		newEndpoint.setLineNumbers(1, lineCount);
	                        }
                        }

                        StrutsEndpoint postVariant = new StrutsEndpoint(makeRelativePath(classLocation, project), path, "POST", parameters);
                        postVariant.setLineNumbers(newEndpoint.getStartingLineNumber(), newEndpoint.getEndingLineNumber());
                        postVariant.setDisplayFilePath(newEndpoint.getDisplayFilePath());

                        endpoints.add(newEndpoint);
                        endpoints.add(postVariant);
                    }
                }

                //  Map actions to their results if they don't specify a target method/class on their own
                Collection<StrutsResult> results = strutsAction.getResults();
                if (results != null && classForAction == null && availablePaths.isEmpty()) {
                    for (StrutsResult result : results) {
                        String filePath = result.getValue();

                        if (filePath != null && primaryWebPack != null) {
                            if (primaryWebPack.contains(filePath)) {
                                String fullContentPath = PathUtil.combine(primaryWebPack.getRootDirectoryPath(), filePath);
                                StrutsEndpoint newEndpoint = new StrutsEndpoint(makeRelativePath(fullContentPath, project), basePath, "GET", new HashMap<String, RouteParameter>());
                                int numLines = CodeParseUtil.countLines(fullContentPath);
                                if (numLines > 0) {
                                	newEndpoint.setLineNumbers(1, numLines);
                                }
                                newEndpoint.setDisplayFilePath(fullContentPath);
                                endpoints.add(newEndpoint);
                            }
                        }
                    }
                }
            }
        }

        return endpoints;
    }

    String makeRelativePath(String path, StrutsProject project) {
        if (path == null) {
            path = "";
        }
        String rootDirectory = project.getRootDirectory();
        if (rootDirectory != null) rootDirectory = FilePathUtils.normalizePath(rootDirectory);

        if (rootDirectory != null && path.startsWith(rootDirectory)) {
            return FilePathUtils.getRelativePath(path, rootDirectory);
        } else {
            return path;
        }
    }

}
