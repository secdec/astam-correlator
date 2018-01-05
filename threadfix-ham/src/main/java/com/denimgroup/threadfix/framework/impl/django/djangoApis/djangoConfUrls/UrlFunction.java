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


package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls;

import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonObject;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonValue;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonVariable;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class UrlFunction extends PythonFunction {

    @Override
    public String getName() {
        return "url";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public List<String> getParams() {
        return list("pattern", "view", "name");
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        PythonObject result = new PythonObject();

        result.setMemberValue("pattern", params[0]);

        PythonValue view = params[1];
        while (view instanceof PythonVariable && ((PythonVariable) view).getValue() != null) {
            view = ((PythonVariable) view).getValue();
        }
        result.setRawMemberValue("view", view);

        return result;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new UrlFunction());
    }
}
