package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class PythonBinaryExpression extends PythonUnaryExpression {
    List<PythonValue> operands = list();

    AbstractPythonStatement sourceLocation;

    public void addOperand(PythonValue operand) {
        this.operands.add(operand);
    }

    public List<PythonValue> getOperands() {
        return this.operands;
    }

    public PythonValue getOperand(int idx) {
        return this.operands.get(idx);
    }

    public int numOperands() {
        return this.operands.size();
    }

    public void setOperands(List<PythonValue> operands) {
        this.operands = new ArrayList<PythonValue>(operands);
    }

    protected abstract void addPrivateSubValues(List<PythonValue> targetList);

    protected boolean replaceOperand(PythonValue oldOperand, PythonValue newOperand) {
        int idx = operands.indexOf(oldOperand);
        if (idx < 0) {
            return false;
        }
        operands.remove(idx);
        operands.add(idx, newOperand);
        return true;
    }

    @Override
    public List<PythonValue> getSubValues() {
        List<PythonValue> subValues = new ArrayList<PythonValue>(operands);
        subValues.addAll(getSubjects());
        addPrivateSubValues(subValues);
        return subValues;
    }
}
