package com.denimgroup.threadfix.framework.impl.django.python.runtime;

public class PythonStringPrimitive implements PythonValue {

    String value;

    public PythonStringPrimitive() {

    }

    public PythonStringPrimitive(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
