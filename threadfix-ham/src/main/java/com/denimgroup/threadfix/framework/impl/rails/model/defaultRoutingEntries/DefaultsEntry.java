package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;

//  This class does nothing since default route values are not handled right now
public class DefaultsEntry extends AbstractRailsRoutingEntry {
    @Override
    public String getPrimaryPath() {
        return null;
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
        DefaultsEntry clone = new DefaultsEntry();
        cloneChildrenInto(clone);
        return clone;
    }
}
