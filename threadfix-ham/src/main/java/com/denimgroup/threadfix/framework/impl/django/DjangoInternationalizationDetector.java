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

package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonVisitor;

import java.util.Map;

public class DjangoInternationalizationDetector extends AbstractPythonVisitor {

    private boolean i18RefFound = false;

    public boolean isLocalized() {
        return i18RefFound;
    }

    private void detectInScope(AbstractPythonStatement scope) {
        if (i18RefFound) {
            return;
        }
        for (Map.Entry<String, String> entry : scope.getImports().entrySet()) {
            if (entry.getValue().equals("django.utils.translation")) {
                i18RefFound = true;
            }
        }
    }

    @Override
    public void visitAny(AbstractPythonStatement statement) {
        detectInScope(statement);
    }
}
