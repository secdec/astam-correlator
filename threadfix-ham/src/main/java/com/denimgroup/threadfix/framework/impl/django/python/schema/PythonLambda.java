package com.denimgroup.threadfix.framework.impl.django.python.schema;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonLambda extends AbstractPythonStatement {

    String name;
    List<String> paramNames = list();
    String functionBody = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitLambda(this);
        super.accept(visitor);
    }

    public List<String> getParamNames() {
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
        if (functionBody != null) {
            functionBody = functionBody.trim();
        }
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
