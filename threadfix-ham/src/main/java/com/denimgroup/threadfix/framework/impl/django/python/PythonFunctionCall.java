package com.denimgroup.threadfix.framework.impl.django.python;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunctionCall extends AbstractPythonStatement {

    String invokeeName = null;
    String functionName = null;
    List<String> args = list();
    PythonPublicVariable resolvedInvokee;
    PythonFunction resolvedFunction;

    @Override
    public String getName() {
        return "call " + functionName + " on " + invokeeName + " at line " + getSourceCodeLine();
    }

    public String getInvokeeName() {
        return invokeeName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setCall(String invokee, String functionName) {
        this.invokeeName = invokee;
        this.functionName = functionName;
    }

    public void setCall(String functionName) {
        this.invokeeName = null;
        this.functionName = functionName;
    }

    public PythonPublicVariable getResolvedInvokee() {
        return resolvedInvokee;
    }

    public PythonFunction getResolvedFunction() {
        return resolvedFunction;
    }

    public void setResolvedInvokee(PythonPublicVariable resolvedInvokee) {
        this.resolvedInvokee = resolvedInvokee;
    }

    public void setResolvedFunction(PythonFunction resolvedFunction) {
        this.resolvedFunction = resolvedFunction;
    }

    public void addParameter(String value) {
        args.add(value);
    }

    public void setParameters(List<String> args) {
        this.args = new ArrayList<String>(args);
    }

    public List<String> getParameters() {
        return args;
    }
}
