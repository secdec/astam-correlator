package com.denimgroup.threadfix.framework.impl.rails.model;

//  Represents a route entry parameter that is common/shared across multiple route entry types.
//  A RouteCommonParameter implementation should take an entry, a route generated from that
//  entry, and modify the route as appropriate.
public interface RouteCommonParameter {

    void modify(RailsRoutingEntry entry, RailsRoute targetRoute);

}
