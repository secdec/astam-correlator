package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.DrawEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.UnknownEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRouteTreeMapper implements RailsConcreteTreeVisitor {

    RailsConcreteRoutingTree routeTree;
    List<RailsRoute> mappedRoutes = list();

    public RailsConcreteRouteTreeMapper(RailsConcreteRoutingTree routeTree) {
        this.routeTree = routeTree;

        this.applyShorthands();

        this.routeTree.walkTree(this);
        mappedRoutes = mergeDuplicates(mappedRoutes);
    }

    public List<RailsRoute> getMappings() {
        return mappedRoutes;
    }

    @Override
    public void visitEntry(RailsRoutingEntry entry) {
        if (entry instanceof DrawEntry || entry instanceof UnknownEntry) {
            return;
        }

        if (entry.getPrimaryPath() == null && (entry.getPaths() == null || entry.getPaths().size() == 0)) {
            return;
        }

        String controllerName = entry.getControllerName();
        if (controllerName == null) {
            return;
        }

        Collection<PathHttpMethod> subPaths = entry.getPaths();
        if (subPaths == null) {
            return;
        }

        for (PathHttpMethod httpMethod : subPaths)
        {
            RailsRoute route = new RailsRoute();
            if (httpMethod.getControllerName() != null) {
                //  Routes declare their controllers but some route entries declare multiple routes
                //  that may have different controllers. These controllers will be set manually
                //  within the returned path.
                route.setController(httpMethod.getControllerName());
            } else {
                route.setController(controllerName);
            }
            route.setUrl(httpMethod.getPath());
            route.addHttpMethod(httpMethod.getMethod());
            mappedRoutes.add(route);
        }
    }

    List<RailsRoute> mergeDuplicates(Collection<RailsRoute> routes) {
        List<RailsRoute> uniqueRoutes = list();
        Map<String, RailsRoute> endpointRouteMap = new HashMap<String, RailsRoute>();
        for (RailsRoute route : routes) {
            String path = route.getUrl();
            if (endpointRouteMap.containsKey(path)) {
                RailsRoute oldRoute = endpointRouteMap.get(path);
                List<String> oldHttpMethods = oldRoute.getHttpMethods();
                for (String newMethod : route.getHttpMethods()) {
                    if (oldHttpMethods.contains(newMethod)) {
                        oldHttpMethods.add(newMethod);
                    }
                }
            } else {
                endpointRouteMap.put(path, route);
                uniqueRoutes.add(route);
            }
        }
        return uniqueRoutes;
    }


    private void applyShorthands() {
        this.routeTree.walkTree(new RailsConcreteTreeVisitor() {
            @Override
            public void visitEntry(RailsRoutingEntry entry) {
                Collection<RouteShorthand> shorthands = entry.getSupportedShorthands();
                if (shorthands == null) {
                    return;
                }

                for (RouteShorthand shorthand : shorthands) {
                    shorthand.expand(routeTree, entry);
                }
            }
        });
    }
}
