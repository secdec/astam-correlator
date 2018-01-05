////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


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

    protected void cloneOperandsTo(PythonBinaryExpression expression) {
        expression.sourceLocation = this.sourceLocation;
        for (PythonValue operand : operands) {
            expression.operands.add(operand.clone());
        }
    }

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
