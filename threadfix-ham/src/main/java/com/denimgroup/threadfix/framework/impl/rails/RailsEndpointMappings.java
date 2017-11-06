////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointGenerator;
import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoute;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by sgerick on 5/5/2015.
 */
public class RailsEndpointMappings implements EndpointGenerator {

    private static final SanitizedLogger LOG = new SanitizedLogger("RailsParser");

    private List<Endpoint> endpoints;
    private Map<String, RailsRoute> routeMap;
    List<RailsController> railsControllers;

    private File rootDirectory;

    public RailsEndpointMappings(@Nonnull File rootDirectory) {
        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            LOG.error("Root file not found or is not directory. Exiting.");
            return;
        }
        File routesFile = new File(rootDirectory, "/config/routes.rb");
        if (!routesFile.exists()) {
            LOG.error("File /config/routes.rb not found. Exiting.");
            return;
        }

        this.rootDirectory = rootDirectory;

        railsControllers = (List<RailsController>) RailsControllerParser.parse(rootDirectory);

        RailsAbstractRoutesParser abstractRoutesParser = new RailsAbstractRoutesParser();
        EventBasedTokenizerRunner.runRails(routesFile, true, true, abstractRoutesParser);

        RailsConcreteRoutingTreeBuilder treeBuilder = new RailsConcreteRoutingTreeBuilder(list((RailsRouter)new DefaultRailsRouter()));
        RailsConcreteRoutingTree concreteTree = treeBuilder.buildFrom(abstractRoutesParser.getResultTree());

        endpoints = list();

        RailsConcreteRouteTreeMapper concreteMapper = new RailsConcreteRouteTreeMapper(concreteTree);
        Collection<RailsRoute> routes = concreteMapper.getMappings();
        for (RailsRoute route : routes) {
            RailsController controller = getController(route);
            if (controller != null) {
                String controllerPath = getRelativePath(controller.getControllerFile());
                route.setController(controllerPath);

                RailsEndpoint endpoint = new RailsEndpoint(route.getController(), route.getUrl(), route.getHttpMethods(), new HashMap<String, ParameterDataType>());
                endpoints.add(endpoint);
            }
        }
    }

    @Nonnull
    @Override
    public List<Endpoint> generateEndpoints() {
        return endpoints;
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Endpoint> iterator() {
        return endpoints.iterator();
    }

    public String getRelativePath(File f) {
        int rootLength = rootDirectory.getAbsolutePath().length();
        String absFileName = f.getAbsolutePath();
        String relFileName = absFileName.substring(rootLength);
        relFileName = relFileName.replace('\\','/');
        return relFileName;
    }

    private RailsController getController(RailsRoute rr) {

        if (rr.getController() != null) {
            for (RailsController railsController : railsControllers) {
                if (railsController.getControllerName().equalsIgnoreCase(rr.getController())) {
                    return railsController;
                }
            }
        }

        String[] urlFolders = rr.getUrl().split("/");
        ArrayUtils.reverse(urlFolders);
        for (String urlFolder : urlFolders) {
            if (urlFolder.isEmpty())
                continue;
            for (RailsController railsController : railsControllers) {
                String controllerField = railsController.getControllerField();
                if (controllerField.isEmpty())
                    continue;
                if (urlFolder.equalsIgnoreCase(controllerField)) {
                    return railsController;
                }
            }
        }
        for (String urlFolder : urlFolders) {
            if (urlFolder.isEmpty())
                continue;
            for (RailsController railsController : railsControllers) {
                for (RailsControllerMethod railsControllerMethod : railsController.getControllerMethods() ) {
                    String methodName = railsControllerMethod.getMethodName();
                    if (urlFolder.equalsIgnoreCase(methodName)) {
                        return railsController;
                    }
                }
            }
        }
        return null;
    }


}
