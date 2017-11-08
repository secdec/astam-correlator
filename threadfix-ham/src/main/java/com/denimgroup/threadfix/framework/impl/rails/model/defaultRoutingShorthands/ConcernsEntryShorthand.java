package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands;

import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ConcernEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ConcernsEntry;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

//  Expands 'concerns' route entries with the route entries referred to in the concerns.
//  This is implemented as a shorthand transformation since a 'concerns' route entry
//  requires lookups in the tree to be resolved, which isn't always available while
//  the 'concerns' entry is being processed.
public class ConcernsEntryShorthand implements RouteShorthand {
    @Override
    public RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry entry) {
        if (!(entry instanceof ConcernsEntry)) {
            return entry;
        }

        ConcernsEntry concernsEntry = (ConcernsEntry)entry;
        Collection<String> concernIds = concernsEntry.getConcerns();
        Collection<ConcernEntry> allConcerns = sourceTree.findEntriesOfType(ConcernEntry.class);

        List<ConcernEntry> necessaryConcerns = list();

        for (ConcernEntry concern : allConcerns) {
            if (concernIds.contains(concern.getConcernIdSymbol())) {
                necessaryConcerns.add(concern);
            }
        }

        RailsRoutingEntry newBase = null;

        for (ConcernEntry concern : necessaryConcerns) {
            for (RailsRoutingEntry subEntry : concern.getChildren()) {
                RailsRoutingEntry entryCopy = subEntry.cloneEntry();
                entryCopy.setParent(entry);
                if (newBase == null) {
                    newBase = entryCopy;
                }
            }
        }

        return newBase;
    }
}
