package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.ReturnExpression;

public class ReturnInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {

        ReturnExpression returnExpression = (ReturnExpression)expression;

        PythonValue result = null;
        if (returnExpression.numSubjects() > 1) {
            PythonTuple resultTuple = new PythonTuple();
            resultTuple.addEntries(returnExpression.getSubjects());
            result = resultTuple;
        } else if (returnExpression.numSubjects() == 1) {
            result = returnExpression.getSubject(0);
        } else {
            result = new PythonNone();
        }

        return result;
    }
}
