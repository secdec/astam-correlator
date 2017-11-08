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

//  A generic scoping entry that can contextualize sub-entries to a controller
//  and define a base endpoint for sub-routes within it.
public class ScopeEntry extends AbstractRailsRoutingEntry {

    String endpoint = "/";
    String module;
    String controller;

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controller);
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ScopeEntry clone = new ScopeEntry();
        clone.endpoint = endpoint;
        clone.module = module;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("module")) {
            module = value;
        } else if (name.equalsIgnoreCase("path")) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("controller")) {
            controller = value;
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(endpoint);
    }

    @Override
    public String toString() {
        return "scope '" + endpoint + "'";
    }
}
