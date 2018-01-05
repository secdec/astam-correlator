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


package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.FunctionCallInterpreter;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class FunctionCallExpression extends PythonUnaryExpression {

    List<PythonValue> parameters = list();

    public void addParameterValue(PythonValue value) {
        this.parameters.add(value);
    }

    public List<PythonValue> getParameters() {
        return parameters;
    }

    public void setParameters(List<PythonValue> parameters) {
        this.parameters = new ArrayList<PythonValue>(parameters);
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (!replaceSubject(previousValue, newValue)) {
            int paramIndex = this.parameters.indexOf(previousValue);
            if (paramIndex >= 0) {
                this.parameters.remove(paramIndex);
                this.parameters.add(paramIndex, newValue);
            }
        }
    }

    @Override
    public PythonValue clone() {
        FunctionCallExpression clone = new FunctionCallExpression();
        clone.resolveSourceLocation(this.getSourceLocation());
        cloneSubjectsTo(clone);
        for (PythonValue param : parameters) {
            clone.addParameterValue(param.clone());
        }
        return clone;
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {
        targetList.addAll(parameters);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < this.numSubjects(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(this.getSubject(i).toString());
        }

        result.append('(');

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(parameters.get(i).toString());
        }

        result.append(')');

        return result.toString();
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new FunctionCallInterpreter();
    }
}
