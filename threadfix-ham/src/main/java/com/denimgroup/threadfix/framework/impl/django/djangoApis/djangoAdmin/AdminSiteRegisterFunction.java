package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.PythonPublicVariable;

public class AdminSiteRegisterFunction extends PythonFunction {

    AdminSiteClass ownerSite;

    public AdminSiteRegisterFunction(AdminSiteClass adminSite) {
        this.ownerSite = adminSite;
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
    public String invoke(PythonCodeCollection codebase, PythonPublicVariable target, String[] params) {

        if (params.length == 0) {
            return null;
        }

        String modelObject;
        String adminController = null;
        modelObject = params[0];
        if (params.length > 1) {
            adminController = params[1];
        }
        this.ownerSite.register(modelObject, adminController);
        return null;
    }
}
