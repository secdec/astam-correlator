package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.*;

public class AdminSiteRegisterFunction extends PythonFunction {

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
        AdminSiteRegisterFunction clone = new AdminSiteRegisterFunction();
        baseCloneTo(clone);
        return clone;
    }

    @Override
    public String invoke(PythonCodeCollection codebase, PythonFunctionCall context, PythonPublicVariable target, String[] params) {

        if (params.length == 0) {
            return null;
        }

        String modelObject;
        String adminController = null;
        modelObject = params[0].trim();
        if (params.length > 1) {
            adminController = params[1].trim();
        }

        PythonClass modelType = codebase.resolveLocalSymbol(modelObject, context, PythonClass.class);
        if (modelType == null) {
            //assert modelType != null : "Couldn't find the model object named: " + modelObject;
            return null;
        }

        AbstractPythonStatement foundObject = modelType.findChild("Meta");
        if (foundObject == null) {
            //  A "Meta" class isn't always required to define the app_label for the model,
            //  but it is assumed here for simplicity (should be expanded upon)
            return null;
        }

        PythonClass metaType = (PythonClass)foundObject;
        AbstractPythonStatement app_label = metaType.findChild("app_label");
        if (app_label == null) {
            return null;
        }

        String appName = ((PythonPublicVariable)app_label).getValueString();
        if (appName.startsWith("'") || appName.startsWith("\"")) {
            appName = appName.substring(1);
        }
        if (appName.endsWith("'") || appName.endsWith("\"")) {
            appName = appName.substring(0, appName.length() - 1);
        }

        String newEndpoint = "r'/^" + appName + "/^" + modelType.getName().toLowerCase() + "/$'";

        PythonPublicVariable urlsVariable = (PythonPublicVariable)target.findChild("urls");
        if (urlsVariable == null) {
            return null;
        }
        String urls = urlsVariable.getValueString();
        if (urls == null) {
            urls = "[]";
        } else {
            urls = urls.substring(1, urls.length() - 1);
        }


        String controllerName = null;
        if (adminController != null) {
            foundObject = codebase.resolveLocalSymbol(adminController, context);
            if (foundObject != null) {
                controllerName = foundObject.getFullName();
            }
        }

        if (controllerName == null) {
            controllerName = modelObject;
        }

        String newUrl = "url(" + newEndpoint + ", " + controllerName + ")";

        if (urls.length() > 0) {
            urls += ",";
        }

        urls += newUrl;

        urls = "[" + urls + "]";

        urlsVariable.setValueString(urls);


        return null;
    }
}
