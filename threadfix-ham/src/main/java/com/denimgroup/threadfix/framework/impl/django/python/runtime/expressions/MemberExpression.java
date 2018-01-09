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
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonUnaryExpression;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.ExpressionInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters.MemberInterpreter;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class MemberExpression extends PythonUnaryExpression {

    List<String> memberPath = list();

    public void appendPath(String relativePath) {
        this.memberPath.add(relativePath);
    }

    public void removePath(int index) {
        this.memberPath.remove(index);
    }

    public List<String> getMemberPath() {
        return memberPath;
    }

    public String getFullMemberPath() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < memberPath.size(); i++) {
            String part = memberPath.get(i);
            if (i > 0) {
                sb.append('.');
            }
            sb.append(part);
        }
        return sb.toString();
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        replaceSubject(previousValue, newValue);
    }

    @Override
    public PythonValue clone() {
        MemberExpression clone = new MemberExpression();
        clone.resolveSourceLocation(this.getSourceLocation());
        clone.memberPath.addAll(this.memberPath);
        cloneSubjectsTo(clone);
        return clone;
    }

    @Override
    protected void addPrivateSubValues(List<PythonValue> targetList) {

    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (this.numSubjects() > 0) {
            result.append('(');
            for (int i = 0; i < this.numSubjects(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(this.getSubject(i).toString());
            }
            result.append(")");
        }

        result.append('.');
        result.append(getFullMemberPath());

        return result.toString();
    }

    @Override
    public ExpressionInterpreter makeInterpreter() {
        return new MemberInterpreter();
    }
}
