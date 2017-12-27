
package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
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
        self.setMemberValue("urls", new PythonArray());
        self.setMemberValue("register", registerFunctionDecl);

        return self;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteInit());
    }
}
