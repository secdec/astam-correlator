package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

public class RePathFunction extends PythonFunction {

    @Override
    public String getName() {
        return "re_path";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        return super.invoke(host, context, params);
    }
}
