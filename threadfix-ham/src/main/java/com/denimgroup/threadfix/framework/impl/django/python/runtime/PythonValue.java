package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public interface PythonValue {

    List<PythonValue> getSubValues();
    void resolveSubValue(PythonValue previousValue, PythonValue newValue);

    void resolveSourceLocation(AbstractPythonStatement source);
    AbstractPythonStatement getSourceLocation();

}
