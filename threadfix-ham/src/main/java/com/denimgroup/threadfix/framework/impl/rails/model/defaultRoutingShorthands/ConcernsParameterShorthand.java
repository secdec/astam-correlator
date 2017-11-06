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
