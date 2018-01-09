package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class AdminSiteClass extends PythonClass {
    @Override
    public String getName() {
        return "AdminSite";
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteClass());
    }
}
