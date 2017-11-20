package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonScope;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.PythonModule;

public class DjangoAdminApi extends AbstractDjangoApi {
    @Override
    public void apply(PythonCodeCollection codebase) {
        PythonModule admin = makeModulesFromFullName("django.contrib.admin");

        attachModelAdmin(admin);
        attachAdminSite(admin);

        AbstractPythonScope result = getRootScope(admin);
        tryAddScopes(codebase, result);
    }

    private void attachModelAdmin(AbstractPythonScope target) {

    }

    private void attachAdminSite(AbstractPythonScope target) {

    }


}
