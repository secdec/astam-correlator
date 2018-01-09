package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;

//  Represents a shorthand where a parameter on a route entry will modify
//  the syntax/scoping tree
public interface RouteShorthand {

    RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry entry);

}
