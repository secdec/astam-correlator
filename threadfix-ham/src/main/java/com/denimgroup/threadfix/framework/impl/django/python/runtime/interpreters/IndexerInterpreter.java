package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndexerExpression;

public class IndexerInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {

        IndexerExpression indexerExpression = (IndexerExpression)expression;

        ExecutionContext executionContext = host.getExecutionContext();

        if (indexerExpression.numSubjects() == 0) {
            return new PythonIndeterminateValue();
        }

        PythonValue subject = indexerExpression.getSubject(0);
        PythonValue operand = indexerExpression.getIndexerValue();

        subject = executionContext.resolveAbsoluteValue(subject);
        operand = executionContext.resolveAbsoluteValue(operand);


        if (subject instanceof PythonArray) {
            PythonArray subjectArray = (PythonArray)subject;
            if (operand instanceof PythonNumericPrimitive) {
                int index = (int)((PythonNumericPrimitive)operand).getValue();
                if (index > ((PythonArray) subject).getEntries().size()) {
                    return new PythonNone();
                } else {
                    return subjectArray.entryAt(index);
                }
            } else {
                return new PythonIndeterminateValue();
            }
        } else if (subject instanceof PythonDictionary) {
            PythonDictionary subjectDictionary = (PythonDictionary)subject;
            return subjectDictionary.get(subject);
        } else {
            return new PythonIndeterminateValue();
        }
    }
}
