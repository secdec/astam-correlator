////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.PrimitiveOperationType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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
                    if (operands.size() > 0) {
                        PythonValue mainOperand = operands.get(0);
                        if (mainOperand instanceof PythonArray) {
                            operands = new ArrayList<PythonValue>(((PythonArray) mainOperand).getEntries());
                        }
                    } else {
                        operands.add(new PythonTuple());
                    }
                }

                if (subjects.size() != operands.size()) {
                    break;
                }

                for (int i = 0; i < subjects.size(); i++) {
                    PythonValue subject = subjects.get(i);
                    PythonValue operand = executionContext.resolveAbsoluteValue(operands.get(i));

                    String subjectSymbol = InterpreterUtil.tryGetValueSymbol(subject);

                    if (subjectSymbol != null) {
                        if (subject instanceof PythonVariable) {
                            ((PythonVariable) subject).setValue(operand);
                            executionContext.assignSymbolValue(subjectSymbol, subject);
                        } else {
                            executionContext.assignSymbolValue(subjectSymbol, operand);
                        }
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
                    if (!(concatenated instanceof PythonIndeterminateValue)) {
                        executionContext.assignSymbolValue(sourceSymbol, concatenated);
                    }
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
        subject = executionContext.resolveAbsoluteValue(subject);
        operand = executionContext.resolveAbsoluteValue(operand);

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
            List<PythonVariable> subjectEntries = ((PythonArray)subject).getEntries();
            List<PythonVariable> operandEntries = ((PythonArray)operand).getEntries();

            PythonArray combined;
            if (subject instanceof PythonTuple) {
                combined = new PythonTuple();
            } else {
                combined = new PythonArray();
            }

            for (PythonVariable entry : subjectEntries) {
                combined.addEntry(entry.getValue());
            }

            for (PythonVariable entry : operandEntries) {
                combined.addEntry(entry.getValue());
            }

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

        subject = executionContext.resolveAbsoluteValue(subject);
        operand = executionContext.resolveAbsoluteValue(operand);

        if ((subject instanceof PythonNumericPrimitive) && (operand instanceof PythonNumericPrimitive)) {
            double subjectValue = ((PythonNumericPrimitive)subject).getValue();
            double operandValue = ((PythonNumericPrimitive)operand).getValue();
            return new PythonNumericPrimitive(subjectValue - operandValue);
        } else {
            return new PythonIndeterminateValue();
        }
    }

    private PythonValue doStringInterpolation(ExecutionContext executionContext, List<PythonValue> subjects, List<PythonValue> operands) {
        PythonValue subject = executionContext.resolveAbsoluteValue(subjects.get(0));
        String string = tryGetStringValue(subject);

        if (string == null) {
            return new PythonIndeterminateValue();
        }

        for (int i = 0; i < operands.size(); i++) {
            PythonValue operand = executionContext.resolveAbsoluteValue(operands.get(i));
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
