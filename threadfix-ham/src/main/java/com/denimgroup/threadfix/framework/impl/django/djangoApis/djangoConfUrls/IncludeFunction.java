package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

public class IncludeFunction extends PythonFunction {

    @Override
    public String getName() {
        return "include";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        return super.invoke(host, context, params);
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new IncludeFunction());
    }
}
