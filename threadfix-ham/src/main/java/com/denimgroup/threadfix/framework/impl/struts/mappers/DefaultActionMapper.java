package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.model.ModelField;
import com.denimgroup.threadfix.framework.impl.model.ModelFieldSet;
import com.denimgroup.threadfix.framework.impl.struts.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsWebPack;
import com.denimgroup.threadfix.framework.impl.struts.model.*;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.actionEndpointEnumerators.ActionEndpointEnumerator;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.java.EntityParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Resolves struts.xml route/action bindings and forwards unbound queries to direct files in the _webapp_ folder

public class DefaultActionMapper implements ActionMapper {

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String namespace) {

        String allowedActionNames;
        String allowedMethodNames;
        String defaultActionName;
        String defaultMethodName;

        defaultActionName = project.getConfig().get("struts.default.action.name");
        defaultMethodName = project.getConfig().get("struts.default.method.name");
        allowedActionNames = project.getConfig().get("struts.allowed.action.names");
        allowedMethodNames = project.getConfig().get("struts.allowed.method.names");

        String allActionExtensions = project.getConfig().get("struts.action.extension");
        if (allActionExtensions == null) {
            allActionExtensions = "action,";
        }

        String[] actionExtensions = allActionExtensions.split(",", -1);
        StrutsWebPack primaryWebPack = project.findWebPack("/");



        List<StrutsEndpoint> endpoints = list();

        for (StrutsPackage strutsPackage : project.getPackages()) {
            String packageNamespace = strutsPackage.getNamespace();
            if (packageNamespace == null)
                packageNamespace = "/";

            packageNamespace = PathUtil.combine(namespace, packageNamespace);

            if(strutsPackage.getActions().isEmpty()) {
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

                if (strutsAction.getActClass() == null)
                    continue;

                String classLocation = strutsAction.getActClassLocation();
                StrutsClass classForAction = project.findClassByFileLocation(classLocation);
                ModelFieldSet fieldMappings = classForAction.getProperties();
                List<String> httpMethods = list();
                List<String> parameters = list();

                String basePath = sbUrl.toString();

                ActionEndpointEnumerator endpointEnumerator = new ActionEndpointEnumerator();
                Collection<String> availablePaths = endpointEnumerator.getPossibleEndpoints(basePath, actionExtensions);

                for (String path : availablePaths) {
                    if (path.contains("*")) { // wildcard
                        for (StrutsMethod method : classForAction.getMethods()) {
                            path = sbUrl.toString();
                            httpMethods = list();
                            parameters = list();
                            if ("execute".equals(method.getName())) {
                                path = path.replace("!*", "");
                                path = path.replace("*", "");
                                httpMethods.add("GET");
                                endpoints.add(new StrutsEndpoint(classLocation, path, httpMethods, parameters));
                            } else {
                                path = path.replace("*", method.getName());
                                httpMethods.add("POST");
                                for (ModelField mf : fieldMappings) {
                                    parameters.add(mf.getParameterKey());
                                }
                                endpoints.add(new StrutsEndpoint(classLocation, path, httpMethods, parameters));
                            }
                        }
                    } else {
                        httpMethods.add("POST");
                        for (ModelField mf : fieldMappings) {
                            parameters.add(mf.getParameterKey());
                        }
                        endpoints.add(new StrutsEndpoint(classLocation, path, httpMethods, parameters));
                    }
                }

                for (StrutsResult result : strutsAction.getResults()) {
                    String filePath = result.getValue();

                    if (filePath != null && primaryWebPack != null) {
                        if (primaryWebPack.contains(filePath)) {
                            endpoints.add(new StrutsEndpoint(classLocation, PathUtil.combine(basePath, actionName), list("GET"), new ArrayList<String>()));
                        }
                    }
                }
            }
        }

        return endpoints;
    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        return list();
    }

}
