package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.ExecutionContext;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

public class ReturnInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonExpression expression, ExecutionContext context) {
        return null;
    }
}
