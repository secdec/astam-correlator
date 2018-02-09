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

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  See: https://struts.apache.org/docs/restfulactionmapper.html

public class RestPluginActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String namespace) {
        List<StrutsEndpoint> endpoints = list();

        int idIndex = StringUtils.countMatches(namespace, "{id") + 1;

        StrutsConfigurationProperties config = project.getConfig();
        String[] actionExtensions = config.get("struts.action.extension", "action,").split(",", -1);

        String idParamName = config.get("struts.mapper.idParameterName", "id");
        String indexMethodName = config.get("struts.mapper.indexMethodName", "index");
        String getMethodName = config.get("struts.mapper.getMethodName", "show");
        String postMethodName = config.get("struts.mapper.postMethodName", "create");
        String putMethodName = config.get("struts.mapper.putMethodName", "update");
        String deleteMethodName = config.get("struts.mapper.deleteMethodName", "destroy");
        String editMethodName = config.get("struts.mapper.editMethodName", "edit");
        String newMethodName = config.get("struts.mapper.newMethodName", "editNew");

        for (StrutsPackage strutsPackage : packages) {

            if (strutsPackage.getActions() == null) {
                continue;
            }

            StrutsClass sourceClass = strutsPackage.getSourceClass();
            if (sourceClass != null) {
                Collection<String> baseTypes = sourceClass.getBaseTypes();
                boolean isSupported = false;

                for (String type : baseTypes) {
                    if (type.contains("ModelDriven")) {
                        isSupported = true;
                        break;
                    }
                }

                if (!isSupported) {
                    continue;
                }
            }

            String rootEndpoint = strutsPackage.getNamespace();

            for (StrutsAction action : strutsPackage.getActions()) {

                if (action.getActClass() == null || action.getActClassLocation() == null) {
                    continue;
                }

                List<String> possibleMethodNames = list();

                String currentUniqueIdName = idParamName;
                if (idIndex > 1) {
                    currentUniqueIdName += "-" + idIndex;
                }

                String actionMethod = action.getMethod();
                String httpMethod = "GET";
                if (actionMethod.equals(indexMethodName)) {
                    // exposed at base endpoint
                    possibleMethodNames.add("");
                } else if (actionMethod.equals(getMethodName)) {
                    possibleMethodNames.add("/{" + currentUniqueIdName + "}");
                } else if (actionMethod.equals(postMethodName)) {
                    httpMethod = "POST";
                    possibleMethodNames.add("");
                } else if (actionMethod.equals(putMethodName)) {
                    httpMethod = "PUT";
                    possibleMethodNames.add("/{" + currentUniqueIdName + "}");
                } else if (actionMethod.equals(deleteMethodName)) {
                    httpMethod = "DELETE";
                    possibleMethodNames.add("/{" + currentUniqueIdName + "}");
                } else if (actionMethod.equals(editMethodName)) {
                    possibleMethodNames.add("/{" + currentUniqueIdName +"}/edit");
                    possibleMethodNames.add("/{" + currentUniqueIdName + "};edit");
                } else if (actionMethod.equals(newMethodName)) {
                    possibleMethodNames.add("/new");
                } else {
                    possibleMethodNames.add(PathUtil.combine("/{" + currentUniqueIdName + "}", actionMethod));
                }

                Map<String, RouteParameter> params = map();
                if (action.getParams() != null) {
                    for (Map.Entry<String, String> entry : action.getParams().entrySet()) {
                        RouteParameter newParam = new RouteParameter(entry.getKey());
                        newParam.setDataType(entry.getValue());
                        newParam.setParamType(RouteParameterType.QUERY_STRING);
                        params.put(entry.getKey(), newParam);
                    }
                }

                if (httpMethod.equals("DELETE") || httpMethod.equals("PUT")) {

                    RouteParameter methodParam = new RouteParameter("_method");
                    methodParam.setParamType(RouteParameterType.QUERY_STRING);
                    methodParam.setDataType("String");
                    methodParam.setAcceptedValues(list(httpMethod));
                    params.put("_method", methodParam);

                    httpMethod = "POST";
                }

                for (String possibleName : possibleMethodNames) {
                    if (possibleName.endsWith("/")) {
                        possibleName = possibleName.substring(0, possibleName.length() - 1);
                    }
                    for (String extension : actionExtensions) {
                        Map<String, RouteParameter> endpointParams = params;
                        if (possibleName.contains("{")) {
                            endpointParams = map();
                            endpointParams.putAll(params);
                            RouteParameter newParameter = new RouteParameter(currentUniqueIdName);
                            if (endpointParams.containsKey(idParamName)) {
                                RouteParameter existingIdParam = endpointParams.get(idParamName);
                                newParameter.setParamType(RouteParameterType.PARAMETRIC_ENDPOINT);
                                newParameter.setDataType(existingIdParam.getDataTypeSource());
                                endpointParams.remove(idParamName);
                            } else {
                                newParameter.setDataType("String");
                                newParameter.setParamType(RouteParameterType.PARAMETRIC_ENDPOINT);
                            }

                            endpointParams.put(currentUniqueIdName, newParameter);
                        }
                        String url = rootEndpoint;
                        if (possibleName.length() > 0)
                            url = PathUtil.combine(url, possibleName);
                        if (extension.length() > 0)
                            url += "." + extension;
                        String filePath = action.getActClassLocation();
                        if (project.getRootDirectory() != null && filePath.startsWith(project.getRootDirectory())) {
                            filePath = FilePathUtils.getRelativePath(filePath, project.getRootDirectory());
                        }
                        StrutsEndpoint endpoint = new StrutsEndpoint(filePath, url, httpMethod, endpointParams);
                        endpoints.add(endpoint);
                    }
                }
            }
        }

        return endpoints;
    }
}
