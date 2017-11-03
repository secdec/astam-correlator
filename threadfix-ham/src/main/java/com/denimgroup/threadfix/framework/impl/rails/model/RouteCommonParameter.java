package com.denimgroup.threadfix.framework.impl.rails.model;

//  Represents a route entry parameter that is common/shared across multiple route entry types
public interface RouteCommonParameter {

    void modify(RailsRoutingEntry entry);

}
