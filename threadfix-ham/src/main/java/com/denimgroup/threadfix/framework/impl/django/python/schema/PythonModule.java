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

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

public class PythonModule extends AbstractPythonStatement {

    String name;
    Map<String, AbstractPythonStatement> implicitImports = map();


    public Map<String, AbstractPythonStatement> getImplicitImports() {
        return implicitImports;
    }

    public void addImplicitImport(AbstractPythonStatement importedStatement, String alias) {
        implicitImports.put(alias, importedStatement);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonModule clone = new PythonModule();
        baseCloneTo(clone);
        clone.name = this.name;
        return clone;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitModule(this);
        super.accept(visitor);
    }

    @Override
    public AbstractPythonStatement findChild(String immediateChildName) {
        AbstractPythonStatement result = super.findChild(immediateChildName);
        if (result == null) {
            for (Map.Entry<String, AbstractPythonStatement> implicitImport : implicitImports.entrySet()) {
                if (implicitImport.getKey().equals(immediateChildName)) {
                    result = implicitImport.getValue();
                    break;
                }
            }
        }
        return result;
    }
}
