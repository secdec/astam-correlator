////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


package com.denimgroup.threadfix.framework.impl.django.python.schema;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunctionCall extends AbstractPythonStatement {

    String manualName = null;
    String invokeeName = null;
    String assignee = null;
    String functionName = null;
    List<String> args = list();
    PythonPublicVariable resolvedInvokee;
    PythonFunction resolvedFunction;

    @Override
    public String getName() {
        if (manualName == null) {
            return "call " + functionName + " on " + invokeeName + " at line " + getSourceCodeStartLine();
        } else {
            return manualName;
        }
    }

    @Override
    public void setName(String newName) {
        manualName = newName;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonFunctionCall clone = new PythonFunctionCall();
        baseCloneTo(clone);
        clone.manualName = this.manualName;
        clone.invokeeName = this.invokeeName;
        clone.functionName = this.functionName;
        clone.resolvedInvokee = this.resolvedInvokee;
        clone.resolvedFunction = this.resolvedFunction;
        clone.args.addAll(this.args);
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitFunctionCall(this);
        super.accept(visitor);
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
