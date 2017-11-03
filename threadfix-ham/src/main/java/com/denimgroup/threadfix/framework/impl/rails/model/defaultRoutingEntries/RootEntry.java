package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;

import java.util.Collection;

// http://guides.rubyonrails.org/routing.html#using-root

//  Defines the response to use when the root of the current scope is queried.
public class RootEntry extends AbstractRailsRoutingEntry {

    String path = null;

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
        RootEntry clone = new RootEntry();
        clone.path = path;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null || name.equalsIgnoreCase("to")) {
            path = value;
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(path);
    }

    @Override
    public Collection<PathHttpMethod> getSubPaths() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("root to: '");
        result.append(path);
        result.append("'");
        return result.toString();
    }
}
