package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.DrawEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRoutingTreeBuilder implements RailsAbstractTreeVisitor {

    Collection<RailsRouter> routers;

    RailsRoutingEntry currentEntry;
    RailsRoutingEntry lastEntry;
    RailsRoutingEntry currentScope;
    RailsAbstractRouteEntryDescriptor lastDescriptor = null;
    List<RailsAbstractRouteEntryDescriptor> descriptorScopeStack = list();

    public RailsConcreteRoutingTreeBuilder(@Nonnull Collection<RailsRouter> routers) {
        this.routers = routers;
    }

    public RailsConcreteRoutingTree buildFrom(RailsAbstractRoutingTree abstractTree) {
        RailsConcreteRoutingTree concreteTree = new RailsConcreteRoutingTree();

        RailsAbstractRouteEntryDescriptor rootAbstractEntry = abstractTree.getRootDescriptor();
        RailsRoutingEntry rootConcreteEntry = new DrawEntry();

        rootConcreteEntry.onBegin(rootAbstractEntry.getIdentifier());
        concreteTree.setRootEntry(rootConcreteEntry);

        currentScope = rootConcreteEntry;
        currentEntry = rootConcreteEntry;
        lastEntry = rootConcreteEntry;
        lastDescriptor = rootAbstractEntry;
        //descriptorScopeStack.add(rootAbstractEntry);

        abstractTree.walkTree(this);

        if (currentEntry != null) {
            currentEntry.onEnd();
        }

        currentEntry = null;
        currentScope = null;
        lastDescriptor = null;

        return concreteTree;
    }

    private RailsRoutingEntry makeRouteEntry(String identifier, RailsAbstractRouteEntryDescriptor descriptor) {
        RailsRoutingEntry result = null;
        for (RailsRouter router : routers) {
            result = router.identify(identifier);
            if (result != null) break;
        }
        return result;
    }

    @Override
    public void acceptDescriptor(RailsAbstractRouteEntryDescriptor descriptor) {
        if (descriptor == lastDescriptor) {
            return;
        }

        if (lastDescriptor != null) {
            boolean droppedInScope =
                    lastDescriptor.getParentDescriptor() != descriptor.getParentDescriptor()
                            && descriptor.getParentDescriptor() != lastDescriptor;

            if (droppedInScope) {
                RailsAbstractRouteEntryDescriptor currentAbstractScope = descriptorScopeStack.get(descriptorScopeStack.size() - 1);
                while (currentAbstractScope != descriptor.getParentDescriptor() && descriptorScopeStack.size() > 0) {
                    currentScope = currentScope.getParent();
                    descriptorScopeStack.remove(descriptorScopeStack.size() - 1);
                    currentAbstractScope = descriptorScopeStack.get(descriptorScopeStack.size() - 1);
                }
            }
        }

        if (currentEntry != null) {
            currentEntry.onEnd();
        }

        boolean raisedScope = lastDescriptor != null && (descriptor.getParentDescriptor() == lastDescriptor);
        if (raisedScope) {
            currentScope = lastEntry;
            descriptorScopeStack.add(lastDescriptor);
        }

        RailsRoutingEntry entry = makeRouteEntry(descriptor.getIdentifier(), descriptor);
        entry.setLineNumber(descriptor.getLineNumber());
        entry.onBegin(descriptor.getIdentifier());
        entry.setParent(currentScope);
        currentScope.addChildEntry(entry);
        currentEntry = entry;

        lastDescriptor = descriptor;
        lastEntry = currentEntry;
    }

    @Override
    public void acceptParameter(RailsAbstractParameter parameter) {
        currentEntry.onParameter(parameter.getName(), parameter.getValue(), parameter.getParameterType());
    }

    @Override
    public void acceptInitializerParameter(RailsAbstractParameter parameter) {
        currentEntry.onInitializerParameter(parameter.getName(), parameter.getValue(), parameter.getParameterType());
    }
}
