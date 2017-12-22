package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

import java.io.File;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class AdminSiteRegisterFunction extends PythonFunction {

    List<String> params = list("model_or_iterable", "admin_class");

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

        PythonObject self = (PythonObject)executionContext.resolveValue(executionContext.getSelfValue());
        PythonValue modelObject = params[0];
        PythonValue adminController = params.length > 1 ? params[1] : null;

        AbstractPythonStatement modelDecl = modelObject.getSourceLocation();
        AbstractPythonStatement controllerDecl = adminController != null ? adminController.getSourceLocation() : null;

        if (modelDecl == null) {
            return null;
        }

        PythonClass modelMeta = modelDecl.findChild("Meta", PythonClass.class);
        if (modelMeta == null) {
            return null;
        }

        AbstractPythonStatement var_app_label = modelMeta.findChild("app_label");
        String appName = null;

        if (var_app_label != null) {
            appName = ((PythonPublicVariable)var_app_label).getValueString();

            if (appName.startsWith("'") || appName.startsWith("\"")) {
                appName = appName.substring(1);
            }
            if (appName.endsWith("'") || appName.endsWith("\"")) {
                appName = appName.substring(0, appName.length() - 1);
            }
        }

        String baseEndpoint = "r'/^";
        if (appName != null) {
            baseEndpoint += appName + "/^";
        }

        baseEndpoint += modelDecl.getName().toLowerCase();

        String newEndpoint = baseEndpoint + "/$'";

        PythonArray urls = self.getMemberValue("urls", PythonArray.class);
        if (urls == null) {
            return null;
        }

        AbstractPythonStatement urlClass = codebase.findByFullName("django.conf.urls.url");
        PythonObject newUrl = makeUrl(newEndpoint, controllerDecl != null ? controllerDecl : modelDecl);
        newUrl.resolveSourceLocation(urlClass);
        urls.addEntry(newUrl);


        if (controllerDecl != null) {
            PythonFunction getUrlsFunction = controllerDecl.findChild("get_urls", PythonFunction.class);
            if (getUrlsFunction != null) {
                PythonValue controllerInstance = host.run(controllerDecl.getFullName() + "()", controllerDecl);
                PythonValue subUrls = host.run(
                        new File(getUrlsFunction.getSourceCodePath()),
                        getUrlsFunction.getSourceCodeStartLine(),
                        getUrlsFunction.getSourceCodeEndLine(),
                        getUrlsFunction,
                        controllerInstance
                );

                if (subUrls instanceof PythonArray) {

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
        newUrl.setMemberValue("view", viewReference);

        return newUrl;
    }

    //  Disabled for now
//    @Override
//    public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonValue target, PythonValue[] params) {
//
//        if (params.length == 0) {
//            return null;
//        }
//
//        String modelObject;
//        String adminController = null;
//        modelObject = params[0].trim();
//        if (params.length > 1) {
//            adminController = params[1].trim();
//        }
//
//        PythonClass modelType = codebase.resolveLocalSymbol(modelObject, context, PythonClass.class);
//        if (modelType == null) {
//            //assert modelType != null : "Couldn't find the model object named: " + modelObject;
//            return null;
//        }
//
//        AbstractPythonStatement foundObject = modelType.findChild("Meta");
//        if (foundObject == null) {
//            //  A "Meta" class isn't always required to define the app_label for the model,
//            //  but it is assumed here for simplicity (should be expanded upon)
//            return null;
//        }
//
//        PythonClass metaType = (PythonClass)foundObject;
//        AbstractPythonStatement app_label = metaType.findChild("app_label");
//
//        String appName = null;
//
//        if (app_label != null) {
//            appName = ((PythonPublicVariable)app_label).getValueString();
//
//            if (appName.startsWith("'") || appName.startsWith("\"")) {
//                appName = appName.substring(1);
//            }
//            if (appName.endsWith("'") || appName.endsWith("\"")) {
//                appName = appName.substring(0, appName.length() - 1);
//            }
//        }
//
//        String newEndpoint = "r'/^";
//        if (appName != null) {
//            newEndpoint += appName + "/^";
//        }
//
//        newEndpoint += modelType.getName().toLowerCase() + "/$'";
//
//        PythonPublicVariable urlsVariable = (PythonPublicVariable)target.findChild("urls");
//        if (urlsVariable == null) {
//            return null;
//        }
//        String urls = urlsVariable.getValueString();
//        if (urls == null) {
//            urls = "[]";
//        } else {
//            urls = urls.substring(1, urls.length() - 1);
//        }
//
//
//        String controllerName = null;
//        if (adminController != null) {
//            foundObject = codebase.resolveLocalSymbol(adminController, context);
//            if (foundObject != null) {
//                controllerName = foundObject.getFullName();
//            }
//        }
//
//        if (controllerName == null) {
//            controllerName = modelObject;
//        }
//
//        String newUrl = "url(" + newEndpoint + ", " + controllerName + ")";
//
//        if (urls.length() > 0) {
//            urls += ",";
//        }
//
//        urls += newUrl;
//
//        urls = "[" + urls + "]";
//
//        urlsVariable.setValueString(urls);
//
//
//        return null;
//    }
}
