package com.denimgroup.threadfix.framework.impl.django.python.runtime;

public class PythonIndeterminateValue implements PythonValue {

    public static final PythonIndeterminateValue INSTANCE = new PythonIndeterminateValue();

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
