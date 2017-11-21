package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction extends AbstractPythonStatement {
    String name;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    public List<String> getParams() {
        return params;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean canInvoke() {
        return false;
    }

    public String invoke(PythonPublicVariable context, String[] params) {
        return null;
    }

    @Override
    public Map<String, String> getImports() {
        Map<String, String> thisImports = new HashMap<String, String>(super.getImports());
        thisImports.putAll(getParentStatement().getImports());
        return thisImports;
    }

    public PythonClass getOwnerClass() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonClass.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonClass)parent;
        }
    }

    public PythonFunction getOwnerFunction() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonFunction.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonFunction)parent;
        }
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void addParam(String paramName) {
        params.add(paramName);
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
