package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.framework.impl.struts.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.mappers.ActionMapper;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsClass;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  See: https://struts.apache.org/docs/restfulactionmapper.html

public class RestPluginActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String namespace) {
        List<StrutsEndpoint> endpoints = list();

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

                String actionMethod = action.getMethod();
                String httpMethod = "GET";
                if (actionMethod.equals(indexMethodName)) {
                    // exposed at base endpoint
                    possibleMethodNames.add("");
                } else if (actionMethod.equals(getMethodName)) {
                    possibleMethodNames.add("/{" + idParamName + "}");
                } else if (actionMethod.equals(postMethodName)) {
                    httpMethod = "POST";
                    possibleMethodNames.add("");
                } else if (actionMethod.equals(putMethodName)) {
                    httpMethod = "PUT";
                    possibleMethodNames.add("/{" + idParamName + "}");
                } else if (actionMethod.equals(deleteMethodName)) {
                    httpMethod = "DELETE";
                    possibleMethodNames.add("/{" + idParamName + "}");
                } else if (actionMethod.equals(editMethodName)) {
                    possibleMethodNames.add("/{" + idParamName +"}/edit");
                    possibleMethodNames.add("/{" + idParamName + "};edit");
                } else if (actionMethod.equals(newMethodName)) {
                    possibleMethodNames.add("/new");
                } else {
                    possibleMethodNames.add(PathUtil.combine("/{" + idParamName + "}", actionMethod));
                }

                Map<String, ParameterDataType> params = map();
                if (action.getParams() != null) {
                    for (Map.Entry<String, String> entry : action.getParams().entrySet()) {
                        params.put(entry.getKey(), ParameterDataType.getType(entry.getValue()));
                    }
                }

                for (String possibleName : possibleMethodNames) {
                    for (String extension : actionExtensions) {
                        String url = PathUtil.combine(rootEndpoint, possibleName);
                        if (extension.length() > 0)
                            url += "." + extension;
                        StrutsEndpoint endpoint = new StrutsEndpoint(action.getActClassLocation(), url, list(httpMethod), params);
                        endpoints.add(endpoint);
                    }
                }
            }
        }

        return endpoints;
    }
}
