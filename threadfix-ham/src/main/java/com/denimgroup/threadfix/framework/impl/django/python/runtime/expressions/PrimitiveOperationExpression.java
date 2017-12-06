package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.RuntimeUtils;

import java.util.Collection;
import java.util.List;

public class PrimitiveOperationExpression extends PythonBinaryExpression {

    PrimitiveOperationType type = PrimitiveOperationType.UNKNOWN;

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
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public String toString() {
        if (type == PrimitiveOperationType.UNKNOWN) {
            return "<UnknownPrimitiveOperation>";
        } else {
            String separator = null;
            switch (type) {
                case REMOVAL: separator = "-="; break;
                case ADDITION: separator = "+"; break;
                case ASSIGNMENT: separator = "="; break;
                case SUBTRACTION: separator = "-"; break;
                case CONCATENATION: separator = "+="; break;
                case STRING_INTERPOLATION: separator = "%"; break;
            }

            StringBuilder result = new StringBuilder();

            if (!RuntimeUtils.containsExpression(this.getSubjects())) {
                result.append('(');
                for (int i = 0; i < this.getSubjects().size(); i++) {
                    if (i > 0) {
                        result.append(", ");
                    }
                    result.append(this.getSubject(i).toString());
                }
                result.append(')');
            }

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
}
