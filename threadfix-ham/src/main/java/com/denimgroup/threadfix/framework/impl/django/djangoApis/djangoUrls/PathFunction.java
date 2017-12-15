package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

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
    public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonValue target, PythonValue[] params) {
        return super.invoke(codebase, context, target, params);
    }
}
