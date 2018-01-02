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


package com.denimgroup.threadfix.framework.impl.django.python.schema;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonFunction extends AbstractPythonStatement {
    String name;
    List<String> params = list();
    List<PythonDecorator> decorators = list();

    public List<String> getParams() {
        return params;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonFunction clone = new PythonFunction();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.params.addAll(this.params);
        for (PythonDecorator decorator : decorators) {
            clone.addDecorator(decorator.clone());
        }
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitFunction(this);
        super.accept(visitor);
    }

    public boolean canInvoke() {
        return false;
    }

    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        return null;
    }

    @Override
    public Map<String, String> getImports() {
        Map<String, String> thisImports = new HashMap<String, String>(super.getImports());
        thisImports.putAll(getParentStatement().getImports());
        return thisImports;
    }

    public PythonClass getOwnerClass() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonClass.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonClass)parent;
        }
    }

    public PythonFunction getOwnerFunction() {
        AbstractPythonStatement parent = getParentStatement();
        if (parent == null) {
            return null;
        } else if (!PythonFunction.class.isAssignableFrom(parent.getClass())) {
            return null;
        } else {
            return (PythonFunction)parent;
        }
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void addParam(String paramName) {
        params.add(paramName);
    }

    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
