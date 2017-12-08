package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public class PythonNumericPrimitive implements PythonValue {

    double value;
    AbstractPythonStatement sourceLocation;

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
    public void resolveSourceLocation(AbstractPythonStatement source) {
        sourceLocation = source;
    }

    @Override
    public AbstractPythonStatement getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public PythonValue clone() {
        PythonNumericPrimitive clone = new PythonNumericPrimitive(value);
        clone.sourceLocation = this.sourceLocation;
        return clone;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
