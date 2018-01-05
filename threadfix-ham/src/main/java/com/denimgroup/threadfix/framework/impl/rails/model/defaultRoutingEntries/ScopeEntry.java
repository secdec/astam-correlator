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

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;

import javax.annotation.Nonnull;
import java.util.Collection;

// http://guides.rubyonrails.org/routing.html#controller-namespaces-and-routing
// https://devblast.com/b/rails-5-routes-scope-vs-namespace
// https://stackoverflow.com/questions/3029954/difference-between-scope-and-namespace-of-ruby-on-rails-3-routing

//  A generic scoping entry that can contextualize sub-entries to a controller
//  and define a base endpoint for sub-routes within it.
public class ScopeEntry extends AbstractRailsRoutingEntry {

    String endpoint = "/";
    String module;
    String controller;

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controller);
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ScopeEntry clone = new ScopeEntry();
        clone.endpoint = endpoint;
        clone.module = module;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("module")) {
            module = value;
        } else if (name.equalsIgnoreCase("path")) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("controller")) {
            controller = value;
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(endpoint);
    }

    @Override
    public String toString() {
        return "scope '" + endpoint + "'";
    }
}
