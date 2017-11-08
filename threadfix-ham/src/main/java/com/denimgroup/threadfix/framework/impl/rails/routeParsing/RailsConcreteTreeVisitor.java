package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.ListIterator;

public interface RailsConcreteTreeVisitor {

    void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator);

}
