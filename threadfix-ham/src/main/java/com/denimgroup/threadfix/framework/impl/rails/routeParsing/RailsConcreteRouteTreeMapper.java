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

        if (entry.getPrimaryPath() == null && (entry.getSubPaths() == null || entry.getSubPaths().size() == 0)) {
            return;
        }

        String controllerName = resolveControllerName(entry);
        if (controllerName == null) {
            return;
        }

        String primaryPath = entry.getPrimaryPath();
        if (primaryPath != null) {
            RailsRoute primaryRoute = new RailsRoute();
            primaryRoute.setUrl(primaryPath);
            primaryRoute.setController(controllerName);
            primaryRoute.addHttpMethod("GET");
            mappedRoutes.add(primaryRoute);
        }

        Collection<PathHttpMethod> subPaths = entry.getSubPaths();
        if (subPaths == null) {
            return;
        }

        for (PathHttpMethod httpMethod : subPaths)
        {
            RailsRoute route = new RailsRoute();
            route.setController(controllerName);
            route.setUrl(httpMethod.getPath());
            route.addHttpMethod(httpMethod.getMethod());
            mappedRoutes.add(route);
        }
    }

    private String resolveControllerName(RailsRoutingEntry entry) {
        String controllerName = entry.getControllerName();
        while (controllerName == null) {
            entry = entry.getParent();
            if (entry == null)
                break;

            controllerName = entry.getControllerName();
        }
        return controllerName;
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


    private void applyCommonParameters() {

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
