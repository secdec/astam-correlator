package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.ScopingExpression;

public class ScopingInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        ScopingExpression scopingExpression = (ScopingExpression)expression;

        if (scopingExpression.numSubjects() > 1) {
            PythonTuple result = new PythonTuple();
            result.addEntries(scopingExpression.getSubjects());
            return result;
        } else if (scopingExpression.numSubjects() == 1) {
            return scopingExpression.getSubject(0);
        } else {
            return new PythonIndeterminateValue();
        }
    }
}
