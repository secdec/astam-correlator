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

import com.denimgroup.threadfix.framework.impl.django.DjangoProject;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;
import com.denimgroup.threadfix.framework.impl.django.python.runtime.PythonInterpreter;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DjangoApiConfigurator {

    //  Adding support for more standard Django APIs and third-party APIs can be inserted here
    private List<DjangoApi> apis = list(
            (DjangoApi)new DjangoAdminApi(),
            (DjangoApi)new DjangoConfUrlsApi(),
            (DjangoApi)new DjangoUrlsApi()
    );

    public DjangoApiConfigurator(DjangoProject project) {
        for (DjangoApi api : apis) {
            api.configure(project);
        }
    }

    /**
     * Attaches known django API modules, objects, and functions to the given codebase.
     */
    public void applySchema(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchema(codebase);
        }
    }

    public void applySchemaPostLink(PythonCodeCollection codebase) {
        for (DjangoApi api : apis) {
            api.applySchemaPostLink(codebase);
        }
    }

    public void applyRuntime(PythonInterpreter runtime) {
        for (DjangoApi api : apis) {
            api.applyRuntime(runtime);
        }
    }

}
