package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

public class ModelAdminAdminSiteVariable extends PythonPublicVariable {

    @Override
    public String getName() {
        return "admin_site";
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new ModelAdminAdminSiteVariable());
    }
}
