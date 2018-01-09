package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ControllerEntry extends AbstractRailsRoutingEntry {
    String controllerName;

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
        return getParentControllerIfNull(controllerName);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, value, parameterType);
        controllerName = value;
    }


    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ControllerEntry clone = new ControllerEntry();
        clone.controllerName = controllerName;
        cloneChildrenInto(clone);
        return clone;
    }
}
