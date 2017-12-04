package com.denimgroup.threadfix.framework.impl.django.python.runtime;

public class PythonUnresolvedValue implements PythonValue {

    String stringValue;

    public PythonUnresolvedValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
