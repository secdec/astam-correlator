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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonClass extends AbstractPythonStatement {

    List<String> baseTypes = list();
    String name;
    List<PythonDecorator> decorators = list();

    @Override
    public Map<String, String> getImports() {
        Map<String, String> result = new HashMap<String, String>(this.getParentStatement().getImports());
        result.putAll(super.getImports());
        return result;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonClass clone = new PythonClass();
        baseCloneTo(clone);
        clone.name = this.name;
        clone.baseTypes.addAll(this.baseTypes);
        for (PythonDecorator decorator : decorators) {
            clone.addDecorator(decorator.clone());
        }
        return clone;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitClass(this);
        super.accept(visitor);
    }

    public Collection<String> getBaseTypes() {
        return baseTypes;
    }

    public boolean hasBaseType(String typeName) {
        return baseTypes.contains(typeName);
    }

    public void addBaseType(String baseType) {
        baseTypes.add(baseType);
    }

    public void setBaseTypes(Collection<String> baseTypes) {
        this.baseTypes.addAll(baseTypes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }



    public Collection<PythonDecorator> getDecorators() {
        return decorators;
    }

    public void addDecorator(PythonDecorator decorator) {
        decorators.add(decorator);
    }
}
