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
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutesParser;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRouteTreeMapper;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTreeBuilder;
import com.denimgroup.threadfix.framework.impl.rails.routerDetection.DeviseRouterDetector;
import com.denimgroup.threadfix.framework.impl.rails.routerDetection.RouterDetector;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizerRunner;
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

        List<RailsRouter> routers = list();
        routers.add(new DefaultRailsRouter());
        File gemFile = findGemFile(rootDirectory);
        if (gemFile != null) {
            RouterDetector routerDetector = new RouterDetector();
            routers.addAll(routerDetector.detectRouters(gemFile));
        } else {
            LOG.debug("Couldn't find gemfile, skipping router detection");
        }


        RailsConcreteRoutingTreeBuilder treeBuilder = new RailsConcreteRoutingTreeBuilder(routers);
        RailsConcreteRoutingTree concreteTree = treeBuilder.buildFrom(abstractRoutesParser.getResultTree());

        endpoints = list();

        RailsConcreteRouteTreeMapper concreteMapper = new RailsConcreteRouteTreeMapper(concreteTree, true);
        Collection<RailsRoute> routes = concreteMapper.getMappings();
        for (RailsRoute route : routes) {
            RailsController controller = getController(route);
            String controllerPath;
            if (controller != null) {
                controllerPath = getRelativePath(controller.getControllerFile());
            } else {
                controllerPath = route.getController();
            }

            if (controllerPath != null) {
                RailsEndpoint endpoint = new RailsEndpoint(controllerPath, route.getUrl(), route.getHttpMethods(), new HashMap<String, ParameterDataType>());
                endpoints.add(endpoint);
            }
        }
    }

    private String formatRouteModuleName(String fullName) {
        fullName = fullName.replaceAll("::", "/");
        String[] parts = fullName.split("\\/");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) {
                sb.append('/');
            }

            StringBuilder partBuilder = new StringBuilder();
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                if (i == part.length() - 1) {
                    if (Character.isUpperCase(c)) {
                        c = Character.toLowerCase(c);
                    }
                    partBuilder.append(c);
                    continue;
                }

                char cn = part.charAt(i+1);
                if ((Character.isUpperCase(cn) || Character.isDigit(cn)) && Character.isLowerCase(c)) {
                    partBuilder.append(c);
                    partBuilder.append('_');
                } else {
                    if (Character.isUpperCase(c)) {
                        c = Character.toLowerCase(c);
                    }
                    partBuilder.append(c);
                }
            }

            sb.append(partBuilder.toString());
        }
        return sb.toString();
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
            String targetName = rr.getController();
            boolean definesModule = targetName.contains("/");
            String modulePath = null;
            if (definesModule) {
                int controllerNameIndex = targetName.lastIndexOf('/');
                modulePath = targetName.substring(0, controllerNameIndex);
                targetName = targetName.substring(controllerNameIndex + 1);
            }

            for (RailsController railsController : railsControllers) {
                String currentName = railsController.getControllerName();
                if (definesModule) {
                    String controllerModule = railsController.getModuleName();
                    if (controllerModule != null) {
                        controllerModule = formatRouteModuleName(controllerModule);
                    }
                    if (currentName.equalsIgnoreCase(targetName) && modulePath.equalsIgnoreCase(controllerModule)) {
                        return railsController;
                    }
                } else if (currentName.equalsIgnoreCase(targetName)) {
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

    private File findGemFile(File rootDirectory) {
        Collection<File> files = FileUtils.listFiles(rootDirectory, null, true);
        for (File file : files) {
            if (file.getName().equalsIgnoreCase("gemfile")) {
                return file;
            }
        }
        return null;
    }
}
