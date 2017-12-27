package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

public class AdminSiteUrlsVariable extends PythonPublicVariable {
    @Override
    public String getName() {
        return "urls";
    }
}
