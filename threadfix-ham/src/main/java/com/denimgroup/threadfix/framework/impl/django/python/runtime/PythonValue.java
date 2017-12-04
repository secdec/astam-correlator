package com.denimgroup.threadfix.framework.impl.django.python.runtime;

public interface PythonValue {

    void resolveSubValue(PythonValue previousValue, PythonValue newValue);

}
