package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;

public class InterpreterUtil {

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

}
