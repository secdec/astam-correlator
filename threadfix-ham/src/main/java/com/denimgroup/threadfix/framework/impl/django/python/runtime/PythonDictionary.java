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

import com.denimgroup.threadfix.framework.impl.django.python.runtime.expressions.IndeterminateExpression;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonDictionary implements PythonValue {

    Map<PythonValue, PythonVariable> values = map();
    AbstractPythonStatement sourceLocation;

    public void add(PythonValue key, PythonValue value) {
        //  Search for existing key
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (InterpreterUtil.testEquality(value, entry.getKey())) {
                entry.getValue().setValue(value);
                return;
            }
        }

        values.put(key, makeVariable(value));
    }

    public PythonValue get(PythonValue key) {
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (InterpreterUtil.testEquality(entry.getKey(), key)) {
                return entry.getValue();
            }
        }

        return new IndeterminateExpression();
    }

    public boolean testEquality(PythonDictionary other) {
        if (this.values.size() != other.values.size()) {
            return false;
        } else if (other.getClass() != this.getClass()) {
            return false;
        } else {
            for (Map.Entry<PythonValue, PythonVariable> entry : this.values.entrySet()) {
                PythonValue otherValue = other.get(entry.getKey());
                if (otherValue == null) {
                    return false;
                }
                if (!InterpreterUtil.testEquality(entry.getValue(), other)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (values.containsKey(previousValue)) {
            PythonVariable mapValue = values.get(previousValue);
            values.remove(previousValue);
            values.put(newValue, mapValue);
        } else if (values.containsValue(previousValue)) {
            PythonValue key = null;
            for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
                if (entry.getValue() == previousValue) {
                    key = entry.getKey();
                    break;
                }
            }

            if (key != null) {
                values.put(key, makeVariable(newValue));
            }
        }
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
        PythonDictionary clone = new PythonDictionary();
        clone.sourceLocation = this.sourceLocation;
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            clone.values.put(
                    entry.getKey().clone(),
                    makeVariable(entry.getValue().clone())
            );
        }
        return clone;
    }

    @Override
    public List<PythonValue> getSubValues() {
        List<PythonValue> subValues = new ArrayList<PythonValue>(values.size() * 2);
        subValues.addAll(values.keySet());
        subValues.addAll(values.values());
        return subValues;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append('{');

        int i = 0;
        for (Map.Entry<PythonValue, PythonVariable> entry : values.entrySet()) {
            if (i++ > 0) {
                result.append(", ");
            }
            result.append(entry.getKey().toString());
            result.append(": ");
            result.append(entry.getValue().toString());
        }

        result.append('}');

        return result.toString();
    }

    private PythonVariable makeVariable(PythonValue value) {
        if (value instanceof PythonVariable) {
            return (PythonVariable)value.clone();
        } else {
            return new PythonVariable(null, value);
        }
    }
}
