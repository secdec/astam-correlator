package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

import java.util.List;

public class ReturnExpression extends PythonUnaryExpression {

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (!replaceSubject(previousValue, newValue)) {

        }
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("return ");
        if (numSubjects() > 1) {
            result.append('(');
            for (int i = 0; i < numSubjects(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(getSubject(i).toString());
            }
            result.append(')');
        } else if (numSubjects() == 1) {
            result.append(getSubject(0).toString());
        } else {
            result.append("{EMPTY}");
        }

        return result.toString();
    }
}
