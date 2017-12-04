package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;

import java.util.Map;

public class PythonObject implements PythonValue {

    PythonClass classType;

    public PythonObject() {

    }

    public PythonObject(PythonClass classType) {
        this.classType = classType;
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
