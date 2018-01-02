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

package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.framework.impl.rails.model.DefaultRailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.*;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsRoutesParser {

    private List<RailsRouter> routers = list();

    public static List<RailsRoute> run(File routesRb) {
        return run(routesRb, list((RailsRouter)new DefaultRailsRouter()));
    }

    public static List<RailsRoute> run(File routesRb, Collection<RailsRouter> routers) {
        RailsRoutesParser parser = new RailsRoutesParser();
        parser.routers.addAll(routers);
        return parser.parseRoutes(routesRb);
    }


    public List<RailsRoute> parseRoutes(File routesRb) {

        RailsAbstractRoutesLexer lexer = new RailsAbstractRoutesLexer();
        EventBasedTokenizerRunner.runRails(routesRb, true, true, lexer);
        RailsAbstractRoutingTree abstractTree = lexer.getResultTree();

        RailsConcreteRoutingTreeBuilder treeBuilder = new RailsConcreteRoutingTreeBuilder(routers);
        RailsConcreteRoutingTree concreteTree = treeBuilder.buildFrom(abstractTree);

        RailsConcreteRouteTreeMapper routeMapper = new RailsConcreteRouteTreeMapper(concreteTree);
        return routeMapper.getMappings();
    }

    public void addDefaultRouter() {
        routers.add(new DefaultRailsRouter());
    }

    public void addRouter(RailsRouter router) {
        routers.add(router);
    }

    public List<RailsRouter> getRouters() {
        return routers;
    }
}
