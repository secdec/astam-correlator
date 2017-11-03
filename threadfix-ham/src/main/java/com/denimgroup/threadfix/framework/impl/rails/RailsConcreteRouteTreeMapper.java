package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRouteTreeMapper implements RailsConcreteTreeVisitor {

    RailsConcreteRoutingTree routeTree;
    List<StrutsEndpoint> mappedEndpoints = list();

    public RailsConcreteRouteTreeMapper(RailsConcreteRoutingTree routeTree) {
        this.routeTree = routeTree;

        this.applyShorthands();

        this.routeTree.walkTree(this);
    }

    public Collection<StrutsEndpoint> getMappings(RailsConcreteRoutingTree routeTree) {
        return mappedEndpoints;
    }

    @Override
    public void visitEntry(RailsRoutingEntry entry) {

    }



    private void applyCommonParameters() {

    }

    private void applyShorthands() {
        this.routeTree.walkTree(new RailsConcreteTreeVisitor() {
            @Override
            public void visitEntry(RailsRoutingEntry entry) {
                for (RouteShorthand shorthand : entry.getSupportedShorthands()) {
                    shorthand.expand(routeTree, entry);
                }
            }
        });
    }
}
