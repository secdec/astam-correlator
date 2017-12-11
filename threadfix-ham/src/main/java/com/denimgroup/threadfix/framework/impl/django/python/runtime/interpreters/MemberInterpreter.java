package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.MemberExpression;

public class MemberInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        MemberExpression memberExpression = (MemberExpression)expression;
        ExecutionContext executionContext = host.getExecutionContext();

        PythonValue subject = executionContext.resolveValue(memberExpression.getSubject(0));

        if (!(subject instanceof PythonObject)) {
            return new PythonIndeterminateValue();
        }

        PythonObject currentObject = (PythonObject)subject;
        PythonValue selectedValue = null;
        for (String member : memberExpression.getMemberPath()) {
            if (currentObject == null) {
                break;
            }
            if (!currentObject.hasMemberValue(member)) {
                return new PythonIndeterminateValue();
            } else {
                selectedValue = currentObject.getMemberValue(member);
                if (selectedValue instanceof PythonObject) {
                    currentObject = (PythonObject)selectedValue;
                } else {
                    currentObject = null;
                }
            }
        }

        if (selectedValue == null) {
            return new PythonIndeterminateValue();
        } else {
            return null;
        }
    }
}
