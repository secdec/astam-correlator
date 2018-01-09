////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.DrawEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.UnknownEntry;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRouteTreeMapper implements RailsConcreteTreeVisitor {

    static SanitizedLogger LOG = new SanitizedLogger(RailsConcreteRouteTreeMapper.class.getName());

    RailsConcreteRoutingTree routeTree;
    List<RailsRoute> mappedRoutes = list();
    boolean mergeModulesIntoControllers;

    public RailsConcreteRouteTreeMapper(RailsConcreteRoutingTree routeTree) {
        this(routeTree, false);
    }

    public RailsConcreteRouteTreeMapper(RailsConcreteRoutingTree routeTree, boolean mergeModulesIntoControllers) {
        this.routeTree = routeTree;
        this.mergeModulesIntoControllers = mergeModulesIntoControllers;

        this.applyShorthands();

        this.routeTree.walkTree(this);
        mappedRoutes = mergeDuplicates(mappedRoutes);
    }

    public List<RailsRoute> getMappings() {
        return mappedRoutes;
    }

    @Override
    public void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator) {
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

            if (mergeModulesIntoControllers) {
                String modulePath = entry.getModule();
                route.setController(PathUtil.combine(modulePath, route.getController(), false));
            }

            route.setUrl(httpMethod.getPath());
            route.addHttpMethod(httpMethod.getMethod());
            mappedRoutes.add(route);
        }
    }

    List<RailsRoute> mergeDuplicates(Collection<RailsRoute> routes) {
        //  NOTE - This merges routes even if the controller or action names are different! Only
        //              endpoints are checked!
        List<RailsRoute> uniqueRoutes = list();
        Map<String, RailsRoute> endpointRouteMap = new HashMap<String, RailsRoute>();
        for (RailsRoute route : routes) {
            String path = route.getUrl();
            if (endpointRouteMap.containsKey(path)) {
                RailsRoute oldRoute = endpointRouteMap.get(path);
                List<String> oldHttpMethods = oldRoute.getHttpMethods();
                for (String newMethod : route.getHttpMethods()) {
                    if (!oldHttpMethods.contains(newMethod)) {
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
            public void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator) {
                Collection<RouteShorthand> shorthands = entry.getSupportedShorthands();
                if (shorthands == null) {
                    return;
                }

                LOG.debug("Applying shorthands to " + entry.toString() + ", line " + entry.getLineNumber() + " in routes.rb");
                StringBuilder shorthandNames = new StringBuilder();
                for (RouteShorthand shorthand : shorthands) {
                    if (shorthandNames.length() > 0) {
                        shorthandNames.append(", ");
                    }
                    shorthandNames.append(shorthand);
                }
                LOG.debug("Current shorthands: " + shorthandNames.toString());

                RailsRoutingEntry replacementEntry = entry;

                for (RouteShorthand shorthand : shorthands) {
                    if (replacementEntry != entry) {
                        LOG.warn("Route entry has been replaced by a shorthand but another shorthand '" + shorthand.getClass().getName() + "' is being applied afterwards");
                        continue;
                    }
                    LOG.debug("Applying shorthand " + shorthand.getClass().getName());
                    replacementEntry = shorthand.expand(routeTree, entry);
                    if (replacementEntry != entry) {
                        LOG.debug("Current entry was replaced by shorthand " + shorthand.getClass().getName() + " from " + entry.toString() + " to " + replacementEntry.toString());
                    }
                }

                if (replacementEntry != entry) {
                    iterator.set(replacementEntry);
                    iterator.previous();
                }
            }
        });
    }
}
