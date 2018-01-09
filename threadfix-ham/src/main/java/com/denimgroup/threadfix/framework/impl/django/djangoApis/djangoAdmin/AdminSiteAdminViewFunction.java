package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonIndeterminateValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class AdminSiteAdminViewFunction extends PythonFunction {

    @Override
    public String getName() {
        return "admin_view";
    }

    @Override
    public List<String> getParams() {
        return list("func");
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteAdminViewFunction());
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        if (params.length > 0) {
            return params[0];
        } else {
            return new PythonIndeterminateValue();
        }
    }
}
