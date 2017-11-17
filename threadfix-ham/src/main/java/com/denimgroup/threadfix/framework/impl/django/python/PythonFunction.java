package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction extends AbstractPythonScope {
    String name;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    @Override
    public void addImport(String importedItem, String alias) {

    }

    @Override
    public Map<String, String> getImports() {
        return this.findParent(PythonModule.class).getImports();
    }

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

    public PythonClass getOwnerClass() {
        AbstractPythonScope parent = getParentScope();
        if (parent == null) {
            return null;
        } else if (!PythonClass.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonClass)parent;
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
