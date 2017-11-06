package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

public interface RailsRouter {

    RailsRoutingEntry identify(String identifier);

}
