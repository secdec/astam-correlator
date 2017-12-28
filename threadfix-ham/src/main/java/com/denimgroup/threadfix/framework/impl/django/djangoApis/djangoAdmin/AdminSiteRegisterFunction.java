package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.DjangoPathUtil;
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
        return baseCloneTo(new AdminSiteRegisterFunction());
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

        String baseEndpoint = "/^";
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
                StringBuilder constructorCall = new StringBuilder();
                constructorCall.append(controllerDecl.getFullName());
                constructorCall.append("(");
                constructorCall.append(modelDecl.getFullName());
                constructorCall.append(")");

                PythonValue controllerInstance = host.run(constructorCall.toString(), controllerDecl);
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
                        urls.addEntry(entry);
                    }
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
}
