package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoUrls;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonPublicVariable;

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
    public String invoke(PythonCodeCollection codebase, AbstractPythonStatement context, PythonValue target, PythonValue[] params) {
        return super.invoke(codebase, context, target, params);
    }
}
