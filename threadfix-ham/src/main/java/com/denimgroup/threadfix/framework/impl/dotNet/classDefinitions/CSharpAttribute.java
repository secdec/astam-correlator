////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CSharpAttribute {
    private String name;
    private List<CSharpParameter> parameters = list();

    public CSharpAttribute() {

    }

    public CSharpAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CSharpParameter> getParameters() {
        return parameters;
    }

    public void addParameter(CSharpParameter parameter) {
        parameters.add(parameter);
    }

    public CSharpParameter getParameterValue(String parameterName) {
        for (CSharpParameter parameter : parameters) {
            if (parameter.isValue() && parameterName.equals(parameter.getName())) {
                return parameter;
            }
        }

        return null;
    }

    public CSharpParameter getParameterValue(int parameterIndex) {
        if (parameters.size() > parameterIndex) {
            return parameters.get(parameterIndex);
        } else {
            return null;
        }
    }

    public CSharpParameter getParameterValue(String parameterName, int parameterIndex) {
        CSharpParameter result = getParameterValue(parameterName);
        if (result == null) {
            result = getParameterValue(parameterIndex);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (name != null && name.isEmpty()) {
            sb.append("{Unnammed}");
        } else {
            sb.append(name);
        }

        if (!parameters.isEmpty()) {
            sb.append('(');

            boolean isFirst = true;
            for (CSharpParameter param : parameters) {
                if (!isFirst)
                    sb.append(", ");

                sb.append(param.toString());
                isFirst = false;
            }

            sb.append(')');
        }

        sb.append(']');
        return sb.toString();
    }
}
