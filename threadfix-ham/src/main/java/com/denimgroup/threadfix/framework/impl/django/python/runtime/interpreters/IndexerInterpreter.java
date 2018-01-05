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


package com.denimgroup.threadfix.framework.impl.django.python.runtime.interpreters;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndexerExpression;

public class IndexerInterpreter implements ExpressionInterpreter {
    @Override
    public PythonValue interpret(PythonInterpreter host, PythonExpression expression) {

        IndexerExpression indexerExpression = (IndexerExpression)expression;

        ExecutionContext executionContext = host.getExecutionContext();

        if (indexerExpression.numSubjects() == 0) {
            return new PythonIndeterminateValue();
        }

        PythonValue subject = indexerExpression.getSubject(0);
        PythonValue operand = indexerExpression.getIndexerValue();

        subject = executionContext.resolveAbsoluteValue(subject);
        operand = executionContext.resolveAbsoluteValue(operand);


        if (subject instanceof PythonArray) {
            PythonArray subjectArray = (PythonArray)subject;
            if (operand instanceof PythonNumericPrimitive) {
                int index = (int)((PythonNumericPrimitive)operand).getValue();
                if (index > ((PythonArray) subject).getEntries().size()) {
                    return new PythonNone();
                } else {
                    return subjectArray.entryAt(index);
                }
            } else {
                return new PythonIndeterminateValue();
            }
        } else if (subject instanceof PythonDictionary) {
            PythonDictionary subjectDictionary = (PythonDictionary)subject;
            return subjectDictionary.get(subject);
        } else {
            return new PythonIndeterminateValue();
        }
    }
}
