package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import javax.annotation.Nonnull;
import java.util.Collection;

//  Root entry in a routes.rb file
public class DrawEntry extends AbstractRailsRoutingEntry {

    @Override
    public String getPrimaryPath() {
        return null;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentController();
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Nonnull
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
