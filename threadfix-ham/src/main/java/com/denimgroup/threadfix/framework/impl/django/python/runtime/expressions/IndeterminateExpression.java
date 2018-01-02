package com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonBinaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.List;

public class IndeterminateExpression implements PythonExpression {

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
        IndeterminateExpression clone = new IndeterminateExpression();
        clone.sourceLocation = this.sourceLocation;
        return clone;
    }

    @Override
    public List<PythonValue> getSubValues() {
        return null;
    }

    @Override
    public String toString() {
        return "<IndeterminateExpression>";
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return null;
    }

    @Override
    public int getScopingIndentation() {
        return 0;
    }

    @Override
    public void setScopingIndentation(int indentation) {

    }
}
