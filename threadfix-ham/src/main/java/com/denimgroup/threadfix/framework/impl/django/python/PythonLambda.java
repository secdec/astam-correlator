package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonLambda extends AbstractPythonStatement {

    String name;
    Collection<String> paramNames = list();
    String functionBody = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    public Collection<String> getParamNames() {
        return paramNames;
    }

    public void addParam(String paramName) {
        paramNames.add(paramName);
    }

    public void setParamNames(Collection<String> paramNames) {
        this.paramNames.clear();
        this.paramNames.addAll(paramNames);
    }

    public String getFunctionBody() {
        return functionBody;
    }

    public void setFunctionBody(String functionBody) {
        this.functionBody = functionBody;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonLambda clone = new PythonLambda();
        clone.name = this.name;
        clone.paramNames.addAll(this.paramNames);
        clone.functionBody = this.functionBody;
        baseCloneTo(clone);
        return clone;
    }
}
