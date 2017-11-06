package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

public interface RailsAbstractTreeVisitor {

    void acceptDescriptor(RailsAbstractRoutingDescriptor descriptor);
    void acceptParameter(RailsAbstractParameter parameter);

}
