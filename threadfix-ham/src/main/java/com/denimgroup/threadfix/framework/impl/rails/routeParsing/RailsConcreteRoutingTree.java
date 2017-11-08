package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRoutingTree {
    RailsRoutingEntry rootEntry;
    ListIterator<RailsRoutingEntry> currentIterator = null;

    public RailsRoutingEntry getRootEntry() {
        return rootEntry;
    }

    public void setRootEntry(RailsRoutingEntry rootEntry) {
        this.rootEntry = rootEntry;
    }


    public void walkTree(RailsConcreteTreeVisitor visitor) {
        currentIterator = null;
        walkTree(rootEntry, visitor);
    }

    public void walkTree(RailsRoutingEntry startNode, RailsConcreteTreeVisitor visitor) {
        visitor.visitEntry(startNode, currentIterator);

        List<RailsRoutingEntry> children = startNode.getChildren();
        ListIterator<RailsRoutingEntry> iterator = children.listIterator();

        while (iterator.hasNext()) {
            RailsRoutingEntry node = iterator.next();
            currentIterator = iterator;
            walkTree(node, visitor);
        }
    }


    public <Type extends RailsRoutingEntry> Collection<Type> findEntriesOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsConcreteTreeVisitor()
        {
            @Override
            public void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator) {
                if (type.isAssignableFrom(entry.getClass())) {
                    result.add((Type) entry);
                }
            }
        });

        return result;
    }
}
