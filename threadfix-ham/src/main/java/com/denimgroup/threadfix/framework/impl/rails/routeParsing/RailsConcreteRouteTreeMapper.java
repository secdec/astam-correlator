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
import org.apache.commons.lang3.StringUtils;

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
        //mappedRoutes = mergeDuplicates(mappedRoutes);
    }

    public List<RailsRoute> getMappings() {
        return mappedRoutes;
    }

    @Override
    public void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator) {
        if (entry instanceof DrawEntry || entry instanceof UnknownEntry) {
            return;
        }

        if (!entry.canGenerateEndpoints()) {
            return;
        }

        if (entry.getPrimaryPath() == null && (entry.getPaths() == null || entry.getPaths().size() == 0)) {
            return;
        }

        Collection<PathHttpMethod> subPaths = entry.getPaths();
        if (subPaths == null) {
            return;
        }

        String controllerName = entry.getControllerName();
        if (controllerName == null) {
            if (StringUtils.countMatches(entry.getPrimaryPath(), "/") == 1) {
                //  Entries may have a path but no controller; in this case, the controller
                //  and its method are implied from paths of the form: controller/method
                String[] parts = entry.getPrimaryPath().split("/");
                controllerName = parts[0];
                String action = parts[1];

                for (PathHttpMethod httpMethod : subPaths) {
                    if (httpMethod.getAction() == null) {
                        httpMethod.setAction(action);
                    }
                }
            }
        }


        for (PathHttpMethod httpMethod : subPaths)
        {
        	String path = httpMethod.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        	if (!path.startsWith("/")) {
        		path = "/" + path;
	        }
            RailsRoute route = new RailsRoute(path, httpMethod.getMethod());
            if (httpMethod.getControllerName() != null) {
                //  Routes declare their controllers but some route entries declare multiple routes
                //  that may have different controllers. These controllers will be set manually
                //  within the returned path.
                route.setController(httpMethod.getControllerName(), httpMethod.getAction());
            } else {
                route.setController(controllerName, httpMethod.getAction());
            }

            if (mergeModulesIntoControllers) {
                String modulePath = entry.getModule();
                route.setController(PathUtil.combine(modulePath, route.getController(), false), httpMethod.getAction());
            }

            mappedRoutes.add(route);
        }
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
