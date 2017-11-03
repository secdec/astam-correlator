package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#adding-collection-routes
// https://stackoverflow.com/questions/3028653/difference-between-collection-route-and-member-route-in-ruby-on-rails

//  Collection routes are used within a 'resources' route to add more routes
//  to the pre-existing ones defined by 'resources'. Collection routes
//  are relative to collection endpoints and are not added to instance endpoints.
public class CollectionEntry extends AbstractRailsRoutingEntry {
    String endpoint;
    String controllerName, controllerMethod;

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(endpoint);
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
        return controllerMethod;
    }

    @Override
    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    @Override
    public void setActionMethodName(String actionMethodName) {
        this.controllerMethod = actionMethodName;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("collection (");
        result.append(this.getChildren().size());
        result.append(" subroutes)");
        return result.toString();
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        CollectionEntry clone = new CollectionEntry();
        clone.controllerMethod = this.controllerMethod;
        clone.controllerName = this.controllerName;
        clone.endpoint = this.endpoint;
        cloneChildrenInto(clone);
        return clone;
    }
}
