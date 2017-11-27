package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.PythonClass;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class AdminSiteClass extends PythonClass {
    @Override
    public String getName() {
        return "AdminSite";
    }

    @Override
    public AbstractPythonStatement clone() {
        AdminSiteClass clone = new AdminSiteClass();
        baseCloneTo(clone);
        return clone;
    }
}
