package com.denimgroup.threadfix.framework.impl.django.python.runtime;

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class PythonBinaryExpression implements PythonExpression {
    List<PythonValue> subjects = list(), operands = list();

    public void addOperand(PythonValue operand) {
        this.operands.add(operand);
    }

    public void addSubject(PythonValue subject) {
        this.subjects.add(subject);
    }

    public List<PythonValue> getOperands() {
        return this.operands;
    }

    public List<PythonValue> getSubjects() {
        return this.subjects;
    }

    public PythonValue getOperand(int idx) {
        return this.operands.get(idx);
    }

    public PythonValue getSubject(int idx) {
        return this.subjects.get(idx);
    }

    public int numOperands() {
        return this.operands.size();
    }

    public int numSubjects() {
        return this.subjects.size();
    }

    public void setOperands(List<PythonValue> operands) {
        this.operands = new ArrayList<PythonValue>(operands);
    }

    public void setSubjects(List<PythonValue> subjects) {
        this.subjects = new ArrayList<PythonValue>(subjects);
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

    protected boolean replaceSubject(PythonValue oldSubject, PythonValue newSubject) {
        int idx = subjects.indexOf(oldSubject);
        if (idx < 0) {
            return false;
        }
        subjects.remove(idx);
        subjects.add(idx, newSubject);
        return true;
    }

    @Override
    public final List<PythonValue> getSubValues() {
        List<PythonValue> subValues = new ArrayList<PythonValue>(subjects.size() + operands.size());
        subValues.addAll(subjects);
        subValues.addAll(operands);
        addPrivateSubValues(subValues);
        return subValues;
    }
}
