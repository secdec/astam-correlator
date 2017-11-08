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
