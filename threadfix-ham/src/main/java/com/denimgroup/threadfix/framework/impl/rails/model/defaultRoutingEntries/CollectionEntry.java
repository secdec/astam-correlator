package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;

import java.util.Collection;

// http://guides.rubyonrails.org/routing.html#adding-collection-routes
// https://stackoverflow.com/questions/3028653/difference-between-collection-route-and-member-route-in-ruby-on-rails

//  Collection routes are used within a 'resources' route to add more routes
//  to the pre-existing ones defined by 'resources'. Collection routes
//  are relative to collection endpoints and are not added to instance endpoints.
public class CollectionEntry extends AbstractRailsRoutingEntry {

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
        cloneChildrenInto(clone);
        return clone;
    }
}
