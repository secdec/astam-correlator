package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
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
            newEntry.onParameter(null, symbol, RouteParameterValueType.SYMBOL);
            resultContainer.addChildEntry(newEntry);
        }

        return resultContainer;
    }
}
