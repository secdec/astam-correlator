package com.denimgroup.threadfix.framework.impl.django;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction {
    String methodName;
    PythonClass ownerClass;
    int lineNumber;
    List<String> params = list();

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
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
}
