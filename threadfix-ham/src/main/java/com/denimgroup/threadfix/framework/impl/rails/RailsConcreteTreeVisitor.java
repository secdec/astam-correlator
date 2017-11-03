package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

public interface RailsConcreteTreeVisitor {

    void visitEntry(RailsRoutingEntry entry);

}
