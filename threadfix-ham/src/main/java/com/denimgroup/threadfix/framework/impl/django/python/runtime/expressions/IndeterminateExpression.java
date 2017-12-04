package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

public class IndeterminateExpression implements PythonBinaryExpression {

    public static final IndeterminateExpression INSTANCE = new IndeterminateExpression();

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {

    }
}
