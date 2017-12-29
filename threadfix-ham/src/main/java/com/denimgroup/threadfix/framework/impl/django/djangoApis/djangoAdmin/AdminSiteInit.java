
package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

public class AdminSiteInit extends PythonFunction {

    @Override
    public String getName() {
        return "__init__";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        PythonCodeCollection codebase = host.getExecutionContext().getCodebase();
        PythonFunction registerFunctionDecl = codebase.findByFullName("django.contrib.admin.AdminSite.register", PythonFunction.class);
        PythonObject self = (PythonObject)host.getExecutionContext().getSelfValue();

        PythonArray urls = new PythonArray();

        PythonObject loginUrl = new PythonObject(codebase.findByFullName("django.conf.urls.url", PythonClass.class));
        loginUrl.setMemberValue("pattern", new PythonStringPrimitive("/login^/"));
        loginUrl.setMemberValue("view", (PythonValue)null);
        urls.addEntry(loginUrl);

        PythonObject logoutUrl = new PythonObject(codebase.findByFullName("django.conf.urls.url", PythonClass.class));
        logoutUrl.setMemberValue("pattern", new PythonStringPrimitive("/logout^/"));
        logoutUrl.setMemberValue("view", (PythonValue)null);
        urls.addEntry(logoutUrl);

        self.setMemberValue("urls", urls);
        self.setMemberValue("register", registerFunctionDecl);

        return self;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteInit());
    }
}
