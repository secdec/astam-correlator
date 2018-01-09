package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#using-root

//  Defines the response to use when the root of the current scope is queried.
public class RootEntry extends AbstractRailsRoutingEntry {

    String path = null;
    String controllerName = null;
    String methodName = null;

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controllerName);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Nonnull
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
            path = "/";
            String[] valueParts = value.split("#");
            controllerName = valueParts[0];
            methodName = valueParts[1];
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(path);
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        List<PathHttpMethod> result = list();
        result.add(new PathHttpMethod(makeRelativePathToParent(path), "GET", methodName, controllerName));
        return result;
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
