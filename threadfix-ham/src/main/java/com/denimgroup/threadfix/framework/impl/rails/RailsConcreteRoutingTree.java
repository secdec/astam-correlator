package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRoutingTree {
    RailsRoutingEntry rootEntry;

    public RailsRoutingEntry getRootEntry() {
        return rootEntry;
    }

    public void setRootEntry(RailsRoutingEntry rootEntry) {
        this.rootEntry = rootEntry;
    }


    public void walkTree(RailsConcreteTreeVisitor iterator) {
        walkTree(rootEntry, iterator);
    }

    public void walkTree(RailsRoutingEntry startNode, RailsConcreteTreeVisitor iterator) {
        iterator.visitEntry(startNode);

        for (RailsRoutingEntry node : startNode.getChildren()) {
            walkTree(node, iterator);
        }
    }


    public <Type extends RailsRoutingEntry> Collection<Type> findEntriesOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsConcreteTreeVisitor()
        {
            @Override
            public void visitEntry(RailsRoutingEntry entry) {
                if (type.isAssignableFrom(entry.getClass())) {
                    result.add((Type) entry);
                }
            }
        });

        return result;
    }
}
