package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.PythonExpressionParser;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;

import java.util.LinkedList;
import java.util.List;

public class InterpreterUtil {

    private static final PythonExpressionParser sharedParser = new PythonExpressionParser();
    private static final PythonValueBuilder sharedBuilder = new PythonValueBuilder();

    public static String tryGetValueSymbol(PythonValue value) {
        if (value.getSourceLocation() != null) {
            return value.getSourceLocation().getFullName();
        } else if (value instanceof PythonObject) {
            return ((PythonObject) value).getMemberPath();
        } else if (value instanceof PythonVariable) {
            return ((PythonVariable) value).getLocalName();
        } else {
            return null;
        }
    }

    public static boolean testEquality(PythonValue lhs, PythonValue rhs) {
        if (lhs == rhs) {
            return true;
        }

        if (lhs instanceof PythonVariable) {
            lhs = ((PythonVariable)lhs).getValue();
        }

        if (rhs instanceof PythonVariable) {
            rhs = ((PythonVariable)rhs).getValue();
        }

        if (lhs == rhs) {
            return true;
        }

        if (lhs.getClass() != rhs.getClass()) {
            return false;
        } else {
            if (lhs instanceof PythonObject) {
                return false; // Python objects can only be compared by reference
            } else if (lhs instanceof PythonArray) {
                return ((PythonArray)lhs).testEquality((PythonArray)rhs);
            } else if (lhs instanceof PythonDictionary) {
                return ((PythonDictionary)lhs).testEquality((PythonDictionary)rhs);
            } else if (lhs instanceof PythonNumericPrimitive) {
                return ((PythonNumericPrimitive)lhs).getValue() == ((PythonNumericPrimitive)rhs).getValue();
            } else if (lhs instanceof PythonStringPrimitive) {
                return ((PythonStringPrimitive)lhs).getValue().equals(((PythonStringPrimitive)rhs).getValue());
            } else if (lhs instanceof PythonNone) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static PythonValue tryMakeValue(String expression, List<PythonValue> expressionSubject) {
        PythonValue asValue = sharedBuilder.buildFromSymbol(expression);
        if (isValidValue(asValue)) {
            return asValue;
        } else {
            return sharedParser.processString(expression, expressionSubject);
        }
    }

    public static boolean isValidValue(PythonValue value) {
        return value != null && !(value instanceof PythonIndeterminateValue);
    }

    public static boolean isValidExpression(PythonExpression expression) {
        return expression != null && !(expression instanceof IndeterminateExpression);
    }

    public static void resolveSubValues(PythonValue value) {
        List<PythonValue> subValues = value.getSubValues();
        if (subValues == null) {
            return;
        } else {
            subValues = new LinkedList<PythonValue>(value.getSubValues());
        }
        while (subValues.size() > 0) {
            PythonValue subValue = subValues.get(0);
            if (subValue instanceof PythonUnresolvedValue) {
                PythonUnresolvedValue unresolvedValue = (PythonUnresolvedValue)subValue;
                PythonValue resolvedValue = tryMakeValue(unresolvedValue.getStringValue(), null);
                if (!(resolvedValue instanceof PythonUnresolvedValue)) {
                    resolvedValue.resolveSourceLocation(unresolvedValue.getSourceLocation());
                    value.resolveSubValue(subValue, resolvedValue);
                    subValue = resolvedValue;
                }
            }

            resolveSubValues(subValue);
            subValues.remove(0);
        }
    }

}
