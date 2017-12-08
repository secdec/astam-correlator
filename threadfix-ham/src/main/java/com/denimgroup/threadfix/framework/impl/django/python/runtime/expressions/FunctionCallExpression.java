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
