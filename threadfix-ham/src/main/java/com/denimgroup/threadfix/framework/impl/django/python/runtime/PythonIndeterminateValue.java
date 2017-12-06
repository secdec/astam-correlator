package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public class PythonIndeterminateValue implements PythonValue {

    public static final PythonIndeterminateValue INSTANCE = new PythonIndeterminateValue();

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }

    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public String toString() {
        return "<IndeterminateValue>";
    }
}
