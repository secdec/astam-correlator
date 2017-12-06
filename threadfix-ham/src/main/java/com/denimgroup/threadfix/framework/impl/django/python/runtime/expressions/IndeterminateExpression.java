package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

import java.util.List;

public class IndeterminateExpression implements PythonExpression {

    public static final IndeterminateExpression INSTANCE = new IndeterminateExpression();

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }

    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public String toString() {
        return "<IndeterminateExpression>";
    }
}
