package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonVariable;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class ModelAdminInit extends PythonFunction {

    List<String> paramNames = list("model", "adminSite");

    @Override
    public String getName() {
        return "__init__";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public List<String> getParams() {
        return paramNames;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {

        PythonCodeCollection codebase = host.getExecutionContext().getCodebase();

        PythonObject self = (PythonObject)host.getExecutionContext().getSelfValue();

        PythonVariable model = (PythonVariable)params[0];
        PythonVariable adminSite = (PythonVariable)params[1];

        if (model.getSourceLocation() == null) {
            return null;
        }

        AbstractPythonStatement modelSource = model.getSourceLocation();
        String modelName = modelSource.getFullName();

        self.setRawMemberValue("model", model);
        self.setMemberValue("opts", host.run(modelName + ".Meta()", modelSource));

        if (adminSite == null) {
            adminSite = (PythonVariable)host.run("django.contrib.admin.site");
        }

        self.setMemberValue("admin_site", adminSite);

        return self;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new ModelAdminInit());
    }
}
