package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

public class InterpreterUtil {

    public static String tryGetValueSymbol(PythonValue value) {
        if (value.getSourceLocation() != null) {
            return value.getSourceLocation().getFullName();
        } else if (value instanceof PythonObject) {
            return ((PythonObject)value).getMemberPath();
        } else {
            return null;
        }
    }

}
