package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationType;

import java.util.List;

public class PrimitiveOperationInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        PrimitiveOperationExpression primitiveOperationExpression = (PrimitiveOperationExpression)expression;

        List<PythonValue> subjects = primitiveOperationExpression.getSubjects();
        List<PythonValue> operands = primitiveOperationExpression.getOperands();
        PrimitiveOperationType type = primitiveOperationExpression.getOperationType();

        PythonValue result = null;

        ExecutionContext executionContext = host.getExecutionContext();

        switch (type) {
            case ASSIGNMENT:

                for (int i = 0; i < subjects.size(); i++) {
                    PythonValue subject = subjects.get(i);
                    PythonValue operand = operands.get(i);

                    String subjectSymbol = InterpreterUtil.tryGetValueSymbol(subject);

                    if (subjectSymbol != null) {
                        executionContext.assignSymbolValue(subjectSymbol, operand);
                        subjects.remove(i);
                        subjects.add(i, operand);
                    }
                }

                if (subjects.size() > 1) {
                    PythonTuple resultTuple = new PythonTuple();
                    for (PythonValue subject : subjects) {
                        resultTuple.addEntry(subject);
                    }
                    result = resultTuple;
                } else {
                    result = subjects.get(0);
                }

                break;

            case REMOVAL:
                break;

            case CONCATENATION:
                break;

            case ADDITION:
                break;

            case SUBTRACTION:
                break;

            case STRING_INTERPOLATION:

                if (subjects.size() == 1) {

                } else {
                    for (int i = 0; i < subjects.size(); i++) {
                        PythonValue subject = subjects.get(i);
                        PythonValue operand = operands.get(i);


                    }
                }

                break;
        }


        return result;
    }
}
