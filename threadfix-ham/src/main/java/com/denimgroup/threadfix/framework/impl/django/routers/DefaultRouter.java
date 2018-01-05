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


package com.denimgroup.threadfix.framework.impl.django.routers;

import com.denimgroup.threadfix.framework.impl.django.DjangoRoute;
import com.denimgroup.threadfix.framework.impl.django.python.schema.AbstractPythonStatement;
import com.denimgroup.threadfix.framework.impl.django.python.PythonCodeCollection;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class DefaultRouter implements DjangoRouter {

    PythonCodeCollection codebase;
    boolean trailingSlash = true;
    List<DjangoRoute> routes = list();

    public DefaultRouter(PythonCodeCollection codebase) {
        this.codebase = codebase;
    }

    @Override
    public String getUrlsName() {
        return "urls";
    }

    @Override
    public void parseConstructorParameters(List<String> parameters) {
        for (String param: parameters) {
            String[] paramValue = param.split("=");
            if (paramValue.length == 2) {
                String name = paramValue[0];
                String value = paramValue[1];

                if (name.equalsIgnoreCase("trailing_slash")) {
                    trailingSlash = value.equalsIgnoreCase("True");
                }
            }
        }
    }

    @Override
    public void parseMethod(String name, List<String> parameters) {
        if (name.equalsIgnoreCase("register")) {
            String path = parameters.get(0);
            if (path.startsWith("r")) {
                path = path.substring(1);
            }

            if (path.startsWith("'") || path.startsWith("\"")) {
                path = path.substring(1);
            }

            if (path.endsWith("'") || path.endsWith("\"")) {
                path = path.substring(0, path.length() - 1);
            }

            String controller = parameters.get(1);
            String viewBaseName = null; // Indicates the text format of related view files, unused
            if (parameters.size() > 2) {
                for (int i = 2; i < parameters.size(); i++) {
                    String param = parameters.get(i);
                    if (param.startsWith("base_name")) {
                        viewBaseName = param.split("\\=")[1];
                    }
                }
            }

            AbstractPythonStatement controllerObj = this.codebase.findByFullName(controller);
            DjangoRoute route;
            if (controllerObj != null) {
                route = new DjangoRoute(path, controllerObj.getSourceCodePath());
                route.setLineNumbers(controllerObj.getSourceCodeStartLine(), controllerObj.getSourceCodeEndLine());
            } else {
                route = new DjangoRoute(path, controller);
            }

            routes.add(route);
        }
    }

    @Override
    public Collection<DjangoRoute> getRoutes() {
        return routes;
    }


}
