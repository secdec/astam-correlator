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
