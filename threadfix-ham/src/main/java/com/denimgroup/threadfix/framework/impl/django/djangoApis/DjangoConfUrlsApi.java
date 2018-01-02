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


package com.denimgroup.threadfix.framework.impl.django.djangoApis;

import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls.IncludeFunction;
import com.denimgroup.threadfix.framework.impl.django.djangoApis.djangoConfUrls.UrlFunction;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;
import com.denimgroup.threadfix.framework.impl.django.python.schema.PythonModule;

public class DjangoConfUrlsApi extends AbstractDjangoApi {
    @Override
    public String getIdentifier() {
        return "django.conf.urls";
    }

    @Override
    public void applySchema(PythonCodeCollection codebase) {

        PythonModule baseModule = makeModulesFromFullName(getIdentifier());

        baseModule.addChildStatement(new UrlFunction());
        baseModule.addChildStatement(new IncludeFunction());

        tryAddScopes(codebase, baseModule);

    }

    @Override
    public void applySchemaPostLink(PythonCodeCollection codebase) {

    }

    @Override
    public void applyRuntime(PythonInterpreter runtime) {

    }
}
