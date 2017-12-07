package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public class PythonUnresolvedValue implements PythonValue {

    String stringValue;
    AbstractPythonStatement sourceLocation;

    public PythonUnresolvedValue(String stringValue) {
        this.stringValue = stringValue.trim();
    }

    public String getStringValue() {
        return stringValue;
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
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public String toString() {
        return "<Unresolved Value {" + stringValue + "}>";
    }
}
