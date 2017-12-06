package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.RuntimeUtils;

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
    protected void addPrivateSubValues(List<PythonValue> targetList) {
        targetList.addAll(parameters);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (!RuntimeUtils.containsExpression(this.getSubjects())) {
            for (int i = 0; i < this.numSubjects(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.getSubject(i).toString());
            }
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
}
