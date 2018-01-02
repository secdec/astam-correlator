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

import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ConcernEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.Concernable;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ConcernsEntry;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;


//  Replaces the "concerns" parameter of a route and appends the concerns routes as children
//  of the attached route
public class ConcernsParameterShorthand implements RouteShorthand {
    @Override
    public RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry entry) {
        if (!(entry instanceof Concernable) || (entry instanceof ConcernsEntry)) {
            return entry;
        }

        Collection<ConcernEntry> allConcerns = sourceTree.findEntriesOfType(ConcernEntry.class);
        Collection<String> concernNames = ((Concernable)entry).getConcerns();
        if (concernNames == null) {
            return entry;
        }

        List<ConcernEntry> neededConcerns = list();
        for (ConcernEntry concern : allConcerns) {
            if (concernNames.contains(concern.getConcernIdSymbol())) {
                neededConcerns.add(concern);
            }
        }

        for (ConcernEntry concern : neededConcerns) {
            Collection<RailsRoutingEntry> concernChildren = concern.getChildren();
            for (RailsRoutingEntry child : concernChildren) {
                RailsRoutingEntry copy = child.cloneEntry();
                copy.setParent(entry);
            }
        }

        ((Concernable) entry).resetConcerns();

        return entry;
    }
}
