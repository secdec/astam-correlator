package com.denimgroup.threadfix.framework.impl.django;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction {
    String name;
    PythonClass ownerClass;
    int lineNumber;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    public PythonFunction() {

    }

    public PythonFunction(PythonClass ownerClass) {
        this.ownerClass = ownerClass;
    }

    public List<String> getParams() {
        return params;
    }

    public PythonClass getOwnerClass() {
        return ownerClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerClass(PythonClass ownerClass) {
        this.ownerClass = ownerClass;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void addParam(String paramName) {
        params.add(paramName);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
