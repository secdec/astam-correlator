package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ScopingInterpreter;

import java.util.List;

public class ScopingExpression extends PythonUnaryExpression {
    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        this.replaceSubject(previousValue, newValue);
    }

    @Override
    public PythonValue clone() {
        ScopingExpression clone = new ScopingExpression();
        clone.resolveSourceLocation(this.getSourceLocation());
        cloneSubjectsTo(clone);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (int i = 0; i < numSubjects(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(getSubject(i).toString());
        }
        result.append(')');
        return result.toString();
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new ScopingInterpreter();
    }
}
