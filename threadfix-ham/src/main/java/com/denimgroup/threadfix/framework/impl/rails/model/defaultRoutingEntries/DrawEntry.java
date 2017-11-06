package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import java.util.Collection;

//  Root entry in a routes.rb file
public class DrawEntry extends AbstractRailsRoutingEntry {

    @Override
    public String getPrimaryPath() {
        return "/";
    }

    @Override
    public Collection<PathHttpMethod> getSubPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return null;
    }

    @Override
    public String getActionMethodName() {
        return null;
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        DrawEntry clone = new DrawEntry();
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public String toString() {
        return "draw";
    }
}
