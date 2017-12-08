package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public class PythonNone implements PythonValue {

    AbstractPythonStatement sourceLocation;

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
        PythonNone clone = new PythonNone();
        clone.sourceLocation = this.sourceLocation;
        return clone;
    }
}
