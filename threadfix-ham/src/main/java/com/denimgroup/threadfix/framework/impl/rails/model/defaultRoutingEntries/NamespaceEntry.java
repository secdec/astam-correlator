package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;

import javax.annotation.Nonnull;
import java.util.Collection;

// http://guides.rubyonrails.org/routing.html#controller-namespaces-and-routing
// https://devblast.com/b/rails-5-routes-scope-vs-namespace
// https://stackoverflow.com/questions/3029954/difference-between-scope-and-namespace-of-ruby-on-rails-3-routing

//  A scope that embeds all sub-routes within its base endpoint.
public class NamespaceEntry extends AbstractRailsRoutingEntry {

    String path = null;
    String controllerName = null;
    String moduleName = null;

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null) {
            path = value;
            moduleName = value;
        } else if (name.equalsIgnoreCase("controller")) {
            controllerName = value;
        } else if (name.equalsIgnoreCase("module")) {
            moduleName = value;
        } else if (name.equalsIgnoreCase("path")) {
            path = value;
        }
    }

    @Override
    public String getModule() {
        return getParentModuleIfNull(moduleName);
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(path);
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controllerName);
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        NamespaceEntry clone = new NamespaceEntry();
        clone.path = path;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("namespace :");
        result.append(path);
        return result.toString();
    }
}
