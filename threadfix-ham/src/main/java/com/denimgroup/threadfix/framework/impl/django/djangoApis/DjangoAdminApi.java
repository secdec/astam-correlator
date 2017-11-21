package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.*;

public class DjangoAdminApi extends AbstractDjangoApi {
    @Override
    public String getIdentifier() {
        return "django.contrib.admin";
    }

    @Override
    public void apply(PythonCodeCollection codebase) {
        PythonModule admin = makeModulesFromFullName("django.contrib.admin");

        attachModelAdmin(admin);
        attachAdminSite(admin);

        AbstractPythonScope result = getRootScope(admin);
        tryAddScopes(codebase, result);
    }

    @Override
    public void applyPostLink(PythonCodeCollection codebase) {

    }

    private void attachModelAdmin(AbstractPythonScope target) {
        PythonClass modelAdmin = new PythonClass();
        modelAdmin.setName("ModelAdmin");

        target.addChildScope(modelAdmin);
    }

    private void attachAdminSite(AbstractPythonScope target) {
        PythonClass adminSite = new PythonClass();
        adminSite.setName("AdminSite");

        PythonFunction register = new PythonFunction();
        register.setName("register");
        adminSite.addChildScope(register);

        target.addChildScope(adminSite);
    }


}
