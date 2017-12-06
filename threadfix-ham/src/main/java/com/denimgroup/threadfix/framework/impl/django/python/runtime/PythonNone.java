package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public class PythonNone implements PythonValue {
    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
