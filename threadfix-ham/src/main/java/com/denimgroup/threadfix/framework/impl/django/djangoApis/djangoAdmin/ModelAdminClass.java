package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;

public class ModelAdminClass extends PythonClass {

    @Override
    public String getName() {
        return "ModelAdmin";
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new ModelAdminClass());
    }
}
