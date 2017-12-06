package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.List;

public interface PythonValue {

    List<PythonValue> getSubValues();
    void resolveSubValue(PythonValue previousValue, PythonValue newValue);

}
