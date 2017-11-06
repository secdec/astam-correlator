package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

// http://guides.rubyonrails.org/routing.html#adding-member-routes
// https://stackoverflow.com/questions/3028653/difference-between-collection-route-and-member-route-in-ruby-on-rails


import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;

//  Member routes are added within a 'resource' or 'resources' scope to make
//  new routes relative to the exposed instances for each route.
public class MemberEntry extends AbstractRailsRoutingEntry {

    String endpoint = null;

    @Override
    public String getPrimaryPath() {
        String basePath = getParent().getPrimaryPath();
        basePath = PathUtil.combine(basePath, ":id");
        return PathUtil.combine(basePath, endpoint);
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
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, value, parameterType);
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        MemberEntry clone = new MemberEntry();
        clone.endpoint = endpoint;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("member (");
        result.append(getChildren().size());
        result.append(" subroutes)");
        return result.toString();
    }
}
