package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;

public class UnknownEntry extends AbstractRailsRoutingEntry {

    String identifier;

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
    public void setControllerName(String controllerName) {

    }

    @Override
    public void setActionMethodName(String actionMethodName) {

    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        UnknownEntry clone = new UnknownEntry();
        clone.identifier = identifier;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public void onBegin(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getPrimaryPath() {
        return null;
    }

    @Override
    public String toString() {
        return identifier + " (unknown entry)";
    }
}
