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

public abstract class PythonUnaryExpression implements PythonExpression {

    List<PythonValue> subjects = list();
    AbstractPythonStatement sourceLocation;
    int indentationLevel = 0;

    public void addSubject(PythonValue subject) {
        this.subjects.add(subject);
    }

    public void setSubjects(List<PythonValue> subjects) {
        this.subjects = new ArrayList<PythonValue>(subjects);
    }

    public int numSubjects() {
        return this.subjects.size();
    }

    public PythonValue getSubject(int idx) {
        return subjects.get(idx);
    }

    public List<PythonValue> getSubjects() {
        return subjects;
    }

    @Override
    public abstract PythonValue clone();

    protected abstract void addPrivateSubValues(List<PythonValue> targetList);

    protected void cloneSubjectsTo(PythonUnaryExpression expression) {
        for (PythonValue subject : subjects) {
            expression.subjects.add(subject.clone());
        }
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
    public List<PythonValue> getSubValues() {
        List<PythonValue> subValues = new ArrayList<PythonValue>(subjects);
        addPrivateSubValues(subValues);
        return subValues;
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
    public int getScopingIndentation() {
        return indentationLevel;
    }

    @Override
    public void setScopingIndentation(int indentation) {
        indentationLevel = indentation;
    }
}
