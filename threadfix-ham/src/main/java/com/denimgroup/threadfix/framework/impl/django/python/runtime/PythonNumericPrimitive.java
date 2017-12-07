package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public class PythonNumericPrimitive implements PythonValue {

    double value;

    public PythonNumericPrimitive() {

    }

    public PythonNumericPrimitive(String value) {
        this.setValue(value);
    }

    public PythonNumericPrimitive(double value) {
        this.setValue(value);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = Double.parseDouble(value);
    }

    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
