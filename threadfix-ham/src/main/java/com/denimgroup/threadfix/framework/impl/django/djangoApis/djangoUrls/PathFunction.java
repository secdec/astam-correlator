package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

public class PathFunction extends PythonFunction {

    @Override
    public String getName() {
        return "path";
    }

    @Override
    public boolean canInvoke() {
        return super.canInvoke();
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        return super.invoke(host, context, params);
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new PathFunction());
    }
}
