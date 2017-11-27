package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.PythonPublicVariable;

public class AdminSiteUrlsVariable extends PythonPublicVariable {

    public AdminSiteUrlsVariable() {
        this.setValueString("[]");
    }

    @Override
    public String getName() {
        return "urls";
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonPublicVariable clone = (PythonPublicVariable)super.clone();
        clone.setName(this.getName());
        clone.setValueString(this.getValueString());
        return clone;
    }
}
