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


package com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoAdmin;

import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.*;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonClass;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonFunction;

public class AdminSiteInit extends PythonFunction {

    @Override
    public String getName() {
        return "__init__";
    }

    @Override
    public boolean canInvoke() {
        return true;
    }

    @Override
    public PythonValue invoke(PythonInterpreter host, AbstractPythonStatement context, PythonValue[] params) {
        PythonCodeCollection codebase = host.getExecutionContext().getCodebase();
        PythonFunction registerFunctionDecl = codebase.findByFullName("django.contrib.admin.AdminSite.register", PythonFunction.class);
        PythonObject self = (PythonObject)host.getExecutionContext().getSelfValue();

        PythonArray urls = new PythonArray();

        PythonObject loginUrl = new PythonObject(codebase.findByFullName("django.conf.urls.url", PythonClass.class));
        loginUrl.setMemberValue("pattern", new PythonStringPrimitive("/login^/"));
        loginUrl.setMemberValue("view", (PythonValue)null);
        urls.addEntry(loginUrl);

        PythonObject logoutUrl = new PythonObject(codebase.findByFullName("django.conf.urls.url", PythonClass.class));
        logoutUrl.setMemberValue("pattern", new PythonStringPrimitive("/logout^/"));
        logoutUrl.setMemberValue("view", (PythonValue)null);
        urls.addEntry(logoutUrl);

        self.setMemberValue("urls", urls);
        self.setMemberValue("register", registerFunctionDecl);

        return self;
    }

    @Override
    public AbstractPythonStatement clone() {
        return baseCloneTo(new AdminSiteInit());
    }
}
