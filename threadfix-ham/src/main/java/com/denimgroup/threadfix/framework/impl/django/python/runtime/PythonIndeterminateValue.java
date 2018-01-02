package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public class PythonIndeterminateValue implements PythonValue {

    public static final PythonIndeterminateValue INSTANCE = new PythonIndeterminateValue();

    AbstractPythonStatement sourceLocation;

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
        PythonIndeterminateValue clone = new PythonIndeterminateValue();
        clone.sourceLocation = this.sourceLocation;
        return clone;
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
