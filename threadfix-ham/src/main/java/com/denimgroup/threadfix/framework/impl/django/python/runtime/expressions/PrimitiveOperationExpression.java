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


package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.PrimitiveOperationInterpreter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class PrimitiveOperationExpression extends PythonBinaryExpression {

    PrimitiveOperationType type = PrimitiveOperationType.UNKNOWN;

    private static final Map<String, PrimitiveOperationType> OPERATORS_MAP = map(
            "+", PrimitiveOperationType.ADDITION,
            "-", PrimitiveOperationType.SUBTRACTION,
            "+=", PrimitiveOperationType.CONCATENATION,
            "-=", PrimitiveOperationType.REMOVAL,
            "=", PrimitiveOperationType.ASSIGNMENT,
            "%", PrimitiveOperationType.STRING_INTERPOLATION,
            "%=", PrimitiveOperationType.STRING_INTERPOLATION_ASSIGNMENT
    );

    public static PrimitiveOperationType interpretOperator(String operator) {
        if (OPERATORS_MAP.containsKey(operator)) {
            return OPERATORS_MAP.get(operator);
        } else {
            return PrimitiveOperationType.UNKNOWN;
        }
    }

    private static Map<PrimitiveOperationType, Integer> OPERATORS_PRIORITY = map(
            PrimitiveOperationType.ADDITION, 1,
            PrimitiveOperationType.SUBTRACTION, 0,
            PrimitiveOperationType.CONCATENATION, -1,
            PrimitiveOperationType.REMOVAL, -2,
            PrimitiveOperationType.ASSIGNMENT, -3,
            PrimitiveOperationType.STRING_INTERPOLATION, 5,
            PrimitiveOperationType.STRING_INTERPOLATION_ASSIGNMENT, -4
    );

    public static PrimitiveOperationExpression rectifyOrderOfOperations(PrimitiveOperationExpression baseExpression) {

        //  Can only rectify OOO for single-var operations
        if (baseExpression.numSubjects() != 1 || baseExpression.numOperands() != 1) {
            return baseExpression;
        }

        //  Sanity check
        if (baseExpression.numSubjects() == 0 || baseExpression.numOperands() == 0) {
            return baseExpression;
        }

        PythonValue subject = baseExpression.getSubject(0);
        PythonValue operand = baseExpression.getOperand(0);

        int basePriority = OPERATORS_PRIORITY.get(baseExpression.getOperationType());
        int subjectPriority = -5;
        int operandPriority = -5;

        if (subject instanceof PrimitiveOperationExpression) {
            PrimitiveOperationExpression subjectOperation = (PrimitiveOperationExpression)subject;
            subjectPriority = OPERATORS_PRIORITY.get(subjectOperation.getOperationType());
            if (basePriority > subjectPriority) {
                swapOrderOfOperations(baseExpression, subjectOperation);
            }

            rectifyOrderOfOperations(subjectOperation);
        }

        if (operand instanceof PrimitiveOperationExpression) {
            PrimitiveOperationExpression operandOperation = (PrimitiveOperationExpression)operand;
            operandPriority = OPERATORS_PRIORITY.get(((PrimitiveOperationExpression) operand).getOperationType());
            if (basePriority > operandPriority) {
                swapOrderOfOperations(operandOperation, baseExpression);
                baseExpression = operandOperation;
                rectifyOrderOfOperations(baseExpression);
            } else {
                rectifyOrderOfOperations(operandOperation);
            }
        }

        return baseExpression;
    }

    private static void swapOrderOfOperations(PrimitiveOperationExpression lhs, PrimitiveOperationExpression rhs) {
        List<PythonValue> lhsSubjects = lhs.getSubjects();
        rhs.setOperands(lhsSubjects);
        lhs.setSubjects(list((PythonValue)rhs));
    }

    public static boolean isAssignment(PrimitiveOperationType operationType) {
        return
                operationType == PrimitiveOperationType.CONCATENATION ||
                operationType == PrimitiveOperationType.REMOVAL ||
                operationType == PrimitiveOperationType.ASSIGNMENT;
    }

    public PrimitiveOperationExpression() {

    }

    public PrimitiveOperationExpression(PrimitiveOperationType type) {
        this.type = type;
    }

    public PrimitiveOperationExpression(PrimitiveOperationType type, PythonValue subject, PythonValue operand) {
        this.type = type;
        this.addSubject(subject);
        this.addOperand(operand);
    }

    public PrimitiveOperationExpression(PrimitiveOperationType type, Collection<PythonValue> subjects, Collection<PythonValue> operands) {
        this.type = type;
        for (PythonValue subject : subjects) {
            this.addSubject(subject);
        }
        for (PythonValue operand : operands) {
            this.addOperand(operand);
        }
    }

    public PrimitiveOperationType getOperationType() {
        return type;
    }

    public void setOperationType(PrimitiveOperationType type) {
        this.type = type;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (!replaceOperand(previousValue, newValue)) {
            replaceSubject(previousValue, newValue);
        }
    }

    @Override
    public PythonValue clone() {
        PrimitiveOperationExpression clone = new PrimitiveOperationExpression();
        clone.type = this.type;
        clone.resolveSourceLocation(this.getSourceLocation());
        cloneSubjectsTo(clone);
        cloneOperandsTo(clone);
        return clone;
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public String toString() {
        if (type == PrimitiveOperationType.UNKNOWN) {
            return "<UnknownPrimitiveOperation>";
        } else {
            String separator = "<UnsupportedPrimitiveOperation>";
            switch (type) {
                case REMOVAL: separator = "-="; break;
                case ADDITION: separator = "+"; break;
                case ASSIGNMENT: separator = "="; break;
                case SUBTRACTION: separator = "-"; break;
                case CONCATENATION: separator = "+="; break;
                case STRING_INTERPOLATION: separator = "%"; break;
                case STRING_INTERPOLATION_ASSIGNMENT: separator = "%="; break;
            }

            StringBuilder result = new StringBuilder();

            result.append('(');
            for (int i = 0; i < this.getSubjects().size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.getSubject(i).toString());
            }
            result.append(')');

            result.append(' ');
            result.append(separator);
            result.append(" (");

            for (int i = 0; i < this.getOperands().size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.getOperand(i).toString());
            }

            result.append(")");
            return result.toString();
        }
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new PrimitiveOperationInterpreter();
    }
}
