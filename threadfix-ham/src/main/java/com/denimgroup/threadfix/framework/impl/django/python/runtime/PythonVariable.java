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

import javax.annotation.Nonnull;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonVariable implements PythonValue {

    private String localName;
    private PythonValue value;
    private PythonValue owner;
    private AbstractPythonStatement sourceLocation;

    public PythonVariable() {

    }

    public PythonVariable(String localName) {
        this.localName = localName;
    }

    public PythonVariable(String localName, PythonValue value) {
        this.localName = localName;
        this.value = value;
    }

    public PythonVariable(AbstractPythonStatement source) {
        this.localName = source.getName();
        this.sourceLocation = source;
    }

    public PythonVariable(String localName, AbstractPythonStatement source) {
        this.localName = localName;
        this.sourceLocation = source;
    }

    public boolean isType(@Nonnull Class<?> valueType) {
        return value != null && valueType.isAssignableFrom(value.getClass());
    }

    public PythonValue getValue() {
        return value;
    }

    public String getLocalName() {
        return localName;
    }

    public PythonValue getOwner() {
        return owner;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Assigns the given value to this variable, resolving the final value
     * if it is a PythonVariable.
     * @param value
     */
    public void setValue(PythonValue value) {
        this.value = value;
        while (this.value instanceof PythonVariable) {
            this.value = ((PythonVariable) this.value).getValue();
        }
    }

    /**
     * Assigns the given value to this variable as-is.
     * @param value
     */
    public void setRawValue(PythonValue value) {
        this.value = value;
    }

    public void setOwner(PythonValue owner) {
        this.owner = owner;
    }

    @Override
    public List<PythonValue> getSubValues() {
        if (value != null) {
            return list(value);
        } else {
            return list();
        }
    }

    @Override
    public void resolveSubValue(PythonValue previousValue, PythonValue newValue) {
        if (value == previousValue) {
            value = newValue;
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
        PythonVariable clone = new PythonVariable(this.localName, this.value);
        clone.sourceLocation = this.sourceLocation;
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (localName != null) {
            result.append(localName);
            result.append("(=");
            result.append(this.value);
            result.append(')');
        } else {
            result.append(this.value);
        }

        return result.toString();
    }
}
