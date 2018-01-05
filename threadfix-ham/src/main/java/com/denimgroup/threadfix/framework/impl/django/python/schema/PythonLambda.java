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
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PythonLambda extends AbstractPythonStatement {

    String name;
    List<String> paramNames = list();
    String functionBody = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        name = newName;
    }

    @Override
    public void accept(AbstractPythonVisitor visitor) {
        visitor.visitLambda(this);
        super.accept(visitor);
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public void addParam(String paramName) {
        paramNames.add(paramName);
    }

    public void setParamNames(Collection<String> paramNames) {
        this.paramNames.clear();
        this.paramNames.addAll(paramNames);
    }

    public String getFunctionBody() {
        return functionBody;
    }

    public void setFunctionBody(String functionBody) {
        if (functionBody != null) {
            functionBody = functionBody.trim();
        }
        this.functionBody = functionBody;
    }

    @Override
    public AbstractPythonStatement clone() {
        PythonLambda clone = new PythonLambda();
        clone.name = this.name;
        clone.paramNames.addAll(this.paramNames);
        clone.functionBody = this.functionBody;
        baseCloneTo(clone);
        return clone;
    }
}
