package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonVariable;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class UrlFunction extends PythonFunction {

    @Override
    public String getName() {
        return "url";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public List<String> getParams() {
        return list("pattern", "view", "name");
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        PythonObject result = new PythonObject();

        result.setMemberValue("pattern", params[0]);

        PythonValue view = params[1];
        while (view instanceof PythonVariable && ((PythonVariable) view).getValue() != null) {
            view = ((PythonVariable) view).getValue();
        }
        result.setRawMemberValue("view", view);

        return result;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new UrlFunction());
    }
}
