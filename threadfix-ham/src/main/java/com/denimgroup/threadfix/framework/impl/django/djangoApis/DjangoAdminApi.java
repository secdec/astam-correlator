package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteClass;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteRegisterFunction;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin.AdminSiteUrlsVariable;
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
        attachGlobalSite(admin);

        AbstractPythonStatement result = getRootScope(admin);
        tryAddScopes(codebase, result);
    }

    @Override
    public void applyPostLink(PythonCodeCollection codebase) {

    }

    private void attachModelAdmin(AbstractPythonStatement target) {
        PythonClass modelAdmin = new PythonClass();
        modelAdmin.setName("ModelAdmin");
        target.addChildStatement(modelAdmin);
    }

    private void attachAdminSite(AbstractPythonStatement target) {
        AdminSiteClass adminSite = new AdminSiteClass();
        PythonFunction register = new AdminSiteRegisterFunction();
        AdminSiteUrlsVariable urls = new AdminSiteUrlsVariable();
        adminSite.addChildStatement(register);
        adminSite.addChildStatement(urls);

        target.addChildStatement(adminSite);
    }

    private void attachGlobalSite(AbstractPythonStatement target) {
        PythonPublicVariable site = new PythonPublicVariable();
        site.setName("site");
        site.setValueString("django.contrib.admin.AdminSite()");

        target.addChildStatement(site);
    }


}
