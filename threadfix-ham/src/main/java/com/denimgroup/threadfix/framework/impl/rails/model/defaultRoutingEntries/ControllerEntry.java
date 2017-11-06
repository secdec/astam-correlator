package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;

import java.util.Collection;

public class ControllerEntry extends AbstractRailsRoutingEntry {
    String controllerName;

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
        return controllerName;
    }

    @Override
    public String getActionMethodName() {
        return null;
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, value, parameterType);
        controllerName = value;
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        ControllerEntry clone = new ControllerEntry();
        clone.controllerName = controllerName;
        cloneChildrenInto(clone);
        return clone;
    }
}
