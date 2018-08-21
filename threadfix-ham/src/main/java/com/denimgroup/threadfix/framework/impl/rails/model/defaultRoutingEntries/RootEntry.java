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

package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#using-root

//  Defines the response to use when the root of the current scope is queried.
public class RootEntry extends AbstractRailsRoutingEntry {

    String path = "";
    String controllerName = null;
    String methodName = null;

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controllerName);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        RootEntry clone = new RootEntry();
        clone.path = path;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType) {
        if (controllerName == null && (name == null || name.equalsIgnoreCase("to"))) {
            String[] valueParts = value.split("#");
            controllerName = valueParts[0];
            methodName = valueParts[1];
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(path);
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        List<PathHttpMethod> result = list();
        result.add(new PathHttpMethod(makeRelativePathToParent(path), "GET", methodName, controllerName));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("root to: '");
        result.append(controllerName);
        result.append("#");
        result.append(methodName);
        result.append("'");
        return result.toString();
    }
}
