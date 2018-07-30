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

package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.RoutingParameterType;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ResourcesEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ScopeEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;

public class ManyResourcesShorthand implements RouteShorthand {
    @Override
    public RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry entry) {
        if (entry.getControllerName() == null || entry.getControllerName().indexOf(' ') < 0) {
            return entry;
        }

        RailsRoutingEntry resultContainer = new ScopeEntry();

        String[] typeSymbols = entry.getControllerName().split(" ");
        for (String symbol : typeSymbols) {
            ResourcesEntry newEntry = new ResourcesEntry();
            //  Many-resource shorthands can only declare routes, doesn't allow
            //      customization of resource routes
            newEntry.onParameter(null, RouteParameterValueType.UNKNOWN, symbol, RouteParameterValueType.SYMBOL);
            newEntry.setLineNumber(entry.getLineNumber());
            resultContainer.addChildEntry(newEntry);
        }

        return resultContainer;
    }
}
