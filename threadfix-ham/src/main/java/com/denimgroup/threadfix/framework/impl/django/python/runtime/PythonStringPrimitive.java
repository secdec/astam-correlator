package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;

import java.util.List;

public class PythonStringPrimitive implements PythonValue {

    String value;
    AbstractPythonStatement sourceLocation;

    public PythonStringPrimitive() {

    }

    public PythonStringPrimitive(String value) {
        setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = CodeParseUtil.trim(value, new String[] { "\"", "'", "r'", "r\"" });
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
        if (this.value != null) {
            return '"' + this.value + '"';
        } else {
            return "<Unassigned String>";
        }
    }
}
