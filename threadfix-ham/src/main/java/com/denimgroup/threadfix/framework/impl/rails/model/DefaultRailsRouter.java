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

package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.*;

public class DefaultRailsRouter implements RailsRouter {

    @Override
    public RailsRoutingEntry identify(String identifier) {
        if (identifier.endsWith(".draw")) {
            return new DrawEntry();
        } else if (identifier.equalsIgnoreCase("get") ||
                identifier.equalsIgnoreCase("put") ||
                identifier.equalsIgnoreCase("post") ||
                identifier.equalsIgnoreCase("delete")) {
            return new DirectHttpEntry();
        } else if (identifier.equalsIgnoreCase("resources")) {
            return new ResourcesEntry();
        } else if (identifier.equalsIgnoreCase("resource")) {
            return new ResourceEntry();
        } else if (identifier.equalsIgnoreCase("match")) {
            return new MatchEntry();
        } else if (identifier.equalsIgnoreCase("namespace")) {
            return new NamespaceEntry();
        } else if (identifier.equalsIgnoreCase("collection")) {
            return new CollectionEntry();
        } else if (identifier.equalsIgnoreCase("concern")) {
            return new ConcernEntry();
        } else if (identifier.equalsIgnoreCase("concerns")) {
            return new ConcernsEntry();
        } else if (identifier.equalsIgnoreCase("member")) {
            return new MemberEntry();
        } else if (identifier.equalsIgnoreCase("root")) {
            return new RootEntry();
        } else if (identifier.equalsIgnoreCase("scope")) {
            return new ScopeEntry();
        } else if (identifier.equalsIgnoreCase("controller")) {
            return new ControllerEntry();
        } else if (identifier.equalsIgnoreCase("defaults")) {
            return new DefaultsEntry();
        } else {
            return new UnknownEntry();
        }
    }

    @Override
    public String resolveController(String controllerPath) {
        return controllerPath;
    }
}
