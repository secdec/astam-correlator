package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

public interface RailsAbstractTreeVisitor {

    void acceptDescriptor(RailsAbstractRouteEntryDescriptor descriptor);
    void acceptParameter(RailsAbstractParameter parameter);
    void acceptInitializerParameter(RailsAbstractParameter parameter);

}
