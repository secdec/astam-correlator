package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PrimitiveOperationInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {
        PrimitiveOperationExpression primitiveOperationExpression = (PrimitiveOperationExpression)expression;

        List<PythonValue> subjects = primitiveOperationExpression.getSubjects();
        List<PythonValue> operands = primitiveOperationExpression.getOperands();
        PrimitiveOperationType type = primitiveOperationExpression.getOperationType();

        PythonValue mainSubject = subjects.get(0);
        String sourceSymbol;

        PythonValue result = null;

        ExecutionContext executionContext = host.getExecutionContext();

        switch (type) {
            case ASSIGNMENT:

                if (subjects.size() > operands.size()) {
                    PythonValue mainOperand = operands.get(0);
                    if (mainOperand instanceof PythonArray) {
                        operands = ((PythonArray)mainOperand).getEntries();
                    }
                }

                for (int i = 0; i < subjects.size(); i++) {
                    PythonValue subject = subjects.get(i);
                    PythonValue operand = executionContext.resolveValue(operands.get(i));

                    String subjectSymbol = InterpreterUtil.tryGetValueSymbol(subject);

                    if (subjectSymbol != null) {
                        executionContext.assignSymbolValue(subjectSymbol, operand);
                        subjects.remove(i);
                        subjects.add(i, operand);
                    }
                }

                if (subjects.size() > 1) {
                    PythonTuple resultTuple = new PythonTuple();
                    resultTuple.addEntries(subjects);
                    result = resultTuple;
                } else {
                    result = subjects.get(0);
                }

                break;

            case REMOVAL:
                PythonValue removed = doMultiSubtraction(executionContext, subjects, operands);
                sourceSymbol = InterpreterUtil.tryGetValueSymbol(mainSubject);
                if (sourceSymbol != null) {
                    executionContext.assignSymbolValue(sourceSymbol, removed);
                    result = removed;
                } else {
                    result = mainSubject;
                }
                break;

            case CONCATENATION:
                PythonValue concatenated = doMultiAddition(executionContext, subjects, operands);
                sourceSymbol = InterpreterUtil.tryGetValueSymbol(mainSubject);
                if (sourceSymbol != null) {
                    executionContext.assignSymbolValue(sourceSymbol, concatenated);
                    result = concatenated;
                } else {
                    result = mainSubject;
                }
                break;

            case ADDITION:
                result = doMultiAddition(executionContext, subjects, operands);
                break;

            case SUBTRACTION:
                result = doMultiSubtraction(executionContext, subjects, operands);
                break;

            case STRING_INTERPOLATION:
                result = doStringInterpolation(executionContext, subjects, operands);
                break;

            case STRING_INTERPOLATION_ASSIGNMENT:
                PythonValue interpolated = doStringInterpolation(executionContext, subjects, operands);

                sourceSymbol = InterpreterUtil.tryGetValueSymbol(mainSubject);
                if (sourceSymbol != null) {
                    executionContext.assignSymbolValue(sourceSymbol, interpolated);
                    result = interpolated;
                } else {
                    result = mainSubject;
                }

                break;
        }


        return result;
    }

    private PythonValue doMultiAddition(ExecutionContext executionContext, List<PythonValue> subjects, List<PythonValue> operands) {
        if (subjects.size() == 1) {
            PythonValue subject = subjects.get(0);
            PythonValue operand = operands.get(0);
            return doAddition(executionContext, subject, operand);
        } else {
            PythonTuple tuple = new PythonTuple();
            for (int i = 0; i < subjects.size(); i++) {
                PythonValue subject = subjects.get(i);
                PythonValue operand = operands.get(i);
                tuple.addEntry(doAddition(executionContext, subject, operand));
            }
            return tuple;
        }
    }

    private PythonValue doAddition(ExecutionContext executionContext, PythonValue subject, PythonValue operand) {
        subject = executionContext.resolveValue(subject);
        operand = executionContext.resolveValue(operand);

        if ((subject instanceof PythonStringPrimitive) && (operand instanceof PythonStringPrimitive)) {
            String subjectString = ((PythonStringPrimitive)subject).getValue();
            String operandString = ((PythonStringPrimitive)operand).getValue();

            if (subjectString == null || operandString == null) {
                return new PythonIndeterminateValue();
            }

            String combined = subjectString + operandString;
            return new PythonStringPrimitive(combined);
        } else if ((subject instanceof PythonNumericPrimitive) && (operand instanceof PythonNumericPrimitive)) {
            double subjectValue = ((PythonNumericPrimitive)subject).getValue();
            double operandValue = ((PythonNumericPrimitive)operand).getValue();
            return new PythonNumericPrimitive(subjectValue + operandValue);
        } else if ((subject instanceof PythonArray) && (operand instanceof PythonArray)) {
            List<PythonValue> subjectEntries = ((PythonArray)subject).getEntries();
            List<PythonValue> operandEntries = ((PythonArray)operand).getEntries();

            PythonArray combined;
            if (subject instanceof PythonTuple) {
                combined = new PythonTuple();
            } else {
                combined = new PythonArray();
            }

            combined.addEntries(subjectEntries);
            combined.addEntries(operandEntries);

            return combined;

        } else {
            return new PythonIndeterminateValue();
        }
    }

    private PythonValue doMultiSubtraction(ExecutionContext executionContext, List<PythonValue> subjects, List<PythonValue> operands) {
        if (subjects.size() == 1) {
            PythonValue subject = subjects.get(0);
            PythonValue operand = operands.get(0);
            return doSubtraction(executionContext, subject, operand);
        } else {
            PythonTuple tuple = new PythonTuple();
            for (int i = 0; i < subjects.size(); i++) {
                PythonValue subject = subjects.get(i);
                PythonValue operand = operands.get(i);
                tuple.addEntry(doSubtraction(executionContext, subject, operand));
            }
            return tuple;
        }
    }

    private PythonValue doSubtraction(ExecutionContext executionContext, PythonValue subject, PythonValue operand) {

        subject = executionContext.resolveValue(subject);
        operand = executionContext.resolveValue(operand);

        if ((subject instanceof PythonNumericPrimitive) && (operand instanceof PythonNumericPrimitive)) {
            double subjectValue = ((PythonNumericPrimitive)subject).getValue();
            double operandValue = ((PythonNumericPrimitive)operand).getValue();
            return new PythonNumericPrimitive(subjectValue + operandValue);
        } else {
            return new PythonIndeterminateValue();
        }
    }

    private PythonValue doStringInterpolation(ExecutionContext executionContext, List<PythonValue> subjects, List<PythonValue> operands) {
        PythonValue subject = executionContext.resolveValue(subjects.get(0));
        String string = tryGetStringValue(subject);

        if (string == null) {
            return new PythonIndeterminateValue();
        }

        for (int i = 0; i < operands.size(); i++) {
            PythonValue operand = executionContext.resolveValue(operands.get(i));
            String operandValue = tryGetStringValue(operand);
            if (operandValue == null) {
                return new PythonIndeterminateValue();
            }

            string = StringUtils.replaceOnce(string, "%s", operandValue);
        }

        return new PythonStringPrimitive(string);
    }

    private String tryGetStringValue(PythonValue value) {
        if (value instanceof PythonStringPrimitive) {
            return ((PythonStringPrimitive)value).getValue();
        } else if (value instanceof PythonNumericPrimitive) {
            double val = ((PythonNumericPrimitive)value).getValue();
            if ((int)val == val) {
                return Integer.toString((int)val);
            } else {
                return Double.toString(val);
            }
        } else {
            return null;
        }
    }
}
