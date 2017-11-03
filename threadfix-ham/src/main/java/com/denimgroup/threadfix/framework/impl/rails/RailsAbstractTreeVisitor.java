package com.denimgroup.threadfix.framework.impl.rails;

public interface RailsAbstractTreeVisitor {

    void acceptDescriptor(RailsAbstractRoutingDescriptor descriptor);
    void acceptParameter(RailsAbstractParameter parameter);

}
