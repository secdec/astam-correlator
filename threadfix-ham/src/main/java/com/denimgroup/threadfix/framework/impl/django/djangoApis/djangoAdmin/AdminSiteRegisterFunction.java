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

package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.DjangoPathUtil;
import com.denimgroup.threadfix.framework.impl.django.DjangoProject;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.*;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class AdminSiteRegisterFunction extends PythonFunction {

    List<String> params = list("model_or_iterable", "admin_class");
    DjangoProject project;

    public AdminSiteRegisterFunction(DjangoProject attachedProject) {
        project = attachedProject;
    }

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteRegisterFunction(project));
    }

    @Override
    public List<String> getParams() {
        return params;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        if (params.length == 0) {
            return null;
        }

        ExecutionContext executionContext = host.getExecutionContext();
        PythonCodeCollection codebase = executionContext.getCodebase();

        PythonObject self = (PythonObject)executionContext.resolveAbsoluteValue(executionContext.getSelfValue());
        PythonValue modelObject = params[0];
        PythonValue adminController = params.length > 1 ? params[1] : null;

        AbstractPythonStatement modelDecl = modelObject.getSourceLocation();
        AbstractPythonStatement controllerDecl = adminController != null ? adminController.getSourceLocation() : null;

        if (modelDecl == null) {
            return null;
        }

        String appName = null;

        PythonClass modelMeta = modelDecl.findChild("Meta", PythonClass.class);
        if (modelMeta != null) {
            AbstractPythonStatement var_app_label = modelMeta.findChild("app_label");

            if (var_app_label != null) {
                appName = ((PythonPublicVariable) var_app_label).getValueString();

                if (appName.startsWith("'") || appName.startsWith("\"")) {
                    appName = appName.substring(1);
                }
                if (appName.endsWith("'") || appName.endsWith("\"")) {
                    appName = appName.substring(0, appName.length() - 1);
                }
            }
        }

        if (appName == null) {
            AbstractPythonStatement appConfigAssignment = modelDecl.getParentStatement().getParentStatement();
            if (appConfigAssignment != null) {

                appConfigAssignment = codebase.findByFullName(appConfigAssignment.getFullName() + ".__init__.default_app_config");

                if (appConfigAssignment != null) {
                    String defaultConfig =
                            appConfigAssignment instanceof PythonPublicVariable ?
                                    ((PythonPublicVariable) appConfigAssignment).getValueString() :
                                    ((PythonVariableModification) appConfigAssignment).getOperatorValue();

                    defaultConfig = CodeParseUtil.trim(defaultConfig, new String[] { "\"", "'" });

                    PythonClass appConfigClass = codebase.findByFullName(defaultConfig, PythonClass.class);
                    if (appConfigClass != null) {

                        PythonPublicVariable labelVar = appConfigClass.findChild("label", PythonPublicVariable.class);
                        PythonPublicVariable nameVar = appConfigClass.findChild("name", PythonPublicVariable.class);

                        if (labelVar != null) {
                            appName = labelVar.getValueString();
                        } else if (nameVar != null) {
                            String fullAppName = nameVar.getValueString();
                            appName = fullAppName.substring(fullAppName.lastIndexOf('.') + 1);
                        }

                        if (appName != null) {
                            appName = CodeParseUtil.trim(appName, new String[] { "\"", "'" });
                        }
                    }
                }
            }
        }

        if (appName == null) {
            String containerApp = findAppForStatement(modelDecl);
            if (containerApp != null) {
                appName = containerApp.substring(containerApp.lastIndexOf('.') + 1);
            }
        }

        String baseEndpoint = "/^";
        if (appName != null) {
            baseEndpoint += appName + "/^";
        }

        baseEndpoint += modelDecl.getName().toLowerCase();

        PythonArray urls = self.getMemberValue("urls", PythonArray.class);
        if (urls == null) {
            return null;
        }

        AbstractPythonStatement urlClass = codebase.findByFullName("django.conf.urls.url");

        AbstractPythonStatement responderDecl = controllerDecl != null ? controllerDecl : modelDecl;

        PythonObject directUrl = makeUrl(baseEndpoint + "/$", responderDecl);
        directUrl.resolveSourceLocation(urlClass);
        urls.addEntry(directUrl);

        String idRegex = "(?<id>(.+))";

        PythonObject changeUrl = makeUrl(DjangoPathUtil.combine(baseEndpoint, idRegex + "/change/$"), responderDecl);
        changeUrl.resolveSourceLocation(urlClass);
        urls.addEntry(changeUrl);

        PythonObject historyUrl = makeUrl(DjangoPathUtil.combine(baseEndpoint, idRegex + "/history/$"), responderDecl);
        historyUrl.resolveSourceLocation(urlClass);
        urls.addEntry(historyUrl);

        PythonObject deleteUrl = makeUrl(DjangoPathUtil.combine(baseEndpoint, idRegex + "/delete/$"), responderDecl);
        deleteUrl.resolveSourceLocation(urlClass);
        urls.addEntry(deleteUrl);


        if (controllerDecl != null) {
            PythonFunction getUrlsFunction = controllerDecl.findChild("get_urls", PythonFunction.class);
            if (getUrlsFunction != null) {
                StringBuilder constructorCall = new StringBuilder();
                constructorCall.append(controllerDecl.getFullName());
                constructorCall.append("(");
                constructorCall.append(modelDecl.getFullName());
                constructorCall.append(")");

                PythonValue controllerInstance = host.run(constructorCall.toString(), controllerDecl, null);
                PythonValue subUrls = host.run(
                        new File(getUrlsFunction.getSourceCodePath()),
                        getUrlsFunction.getSourceCodeStartLine(),
                        getUrlsFunction.getSourceCodeEndLine(),
                        getUrlsFunction,
                        controllerInstance
                );

                subUrls = executionContext.resolveAbsoluteValue(subUrls);

                if (subUrls instanceof PythonArray) {
                    PythonArray urlsArray = (PythonArray)subUrls;
                    for (PythonObject entry : urlsArray.getValues(PythonObject.class)) {
                        PythonStringPrimitive patternVar = entry.getMemberValue("pattern", PythonStringPrimitive.class);
                        if (patternVar != null) {
                            String pattern = DjangoPathUtil.combine(baseEndpoint, patternVar.getValue());
                            patternVar.setValue(pattern);
                            entry.setMemberValue("pattern", patternVar);
                        }
                        entry.resolveSourceLocation(urlClass);
                        urls.addEntry(entry);
                    }
                }
            }
        }

        for (PythonValue value : urls.getValues()) {
            if (!(value instanceof PythonObject)) {
                return null;
            } else {
                PythonObject asObject = (PythonObject)value;
                PythonValue pattern = asObject.getMemberValue("pattern");
                PythonValue view = asObject.getMemberValue("view");

                if (pattern == null || pattern instanceof PythonIndeterminateValue || view instanceof PythonIndeterminateValue) {
                    return null;
                }
            }
        }

        return null;
    }

    private PythonObject makeUrl(String pattern, AbstractPythonStatement controllerSource) {
        PythonObject newUrl = new PythonObject();

        newUrl.setMemberValue("pattern", new PythonStringPrimitive(pattern));

        PythonVariable viewReference = new PythonVariable(controllerSource.getFullName());
        viewReference.resolveSourceLocation(controllerSource);
        newUrl.setRawMemberValue("view", viewReference);

        return newUrl;
    }

    private String findAppForStatement(AbstractPythonStatement statement) {
        String bestApp = null;
        String fullName = statement.getFullName();
        for (String appName : project.getInstalledApps()) {
            if (fullName.startsWith(appName)) {
                if (bestApp == null || appName.length() > bestApp.length()) {
                    bestApp = appName;
                }
            }
        }
        return bestApp;
    }
}
