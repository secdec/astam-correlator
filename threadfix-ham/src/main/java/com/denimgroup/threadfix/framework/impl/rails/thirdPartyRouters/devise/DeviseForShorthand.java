package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.DirectHttpEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ScopeEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;
import com.denimgroup.threadfix.framework.util.PathUtil;
import javafx.scene.effect.SepiaTone;
import javafx.scene.shape.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  Implement devise_for routing as a shorthand to separate module/controller bindings across different entries
public class DeviseForShorthand implements RouteShorthand {

    Map<String, List<PathHttpMethod>> mappedPaths = new HashMap<String, List<PathHttpMethod>>() {
        {
            put("sessions", list(
                    //  Default
                    // http://www.rubydoc.info/github/plataformatec/devise/Devise/SessionsController
                    new PathHttpMethod("sign_in", "GET", "new", "(lib) devise/sessions"),
                    new PathHttpMethod("sign_in", "POST", "create", "(lib) devise/sessions"),
                    new PathHttpMethod("sign_out", "DELETE", "destroy", "(lib) devise/sessions")
            ));

            put("passwords", list(
                    // :recoverable module
                    // http://www.rubydoc.info/github/plataformatec/devise/Devise/PasswordsController
                    new PathHttpMethod("password/new", "GET", "new", "(lib) devise/passwords"),
                    new PathHttpMethod("password/edit", "GET", "edit", "(lib) devise/passwords"),
                    new PathHttpMethod("password", "PUT", "update", "(lib) devise/passwords"),
                    new PathHttpMethod("password", "POST", "create", "(lib) devise/passwords")
            ));

            put("confirmations", list(
                    // :confirmable module
                    // http://www.rubydoc.info/github/plataformatec/devise/Devise/ConfirmationsController
                    new PathHttpMethod("confirmation/new", "GET", "new", "(lib) devise/confirmations"),
                    new PathHttpMethod("confirmation", "GET", "show", "(lib) devise/confirmations"),
                    new PathHttpMethod("confirmation", "POST", "create", "(lib) devise/confirmations")
            ));

            put("registrations", list(
                    // :registerable module
                    // http://www.rubydoc.info/github/plataformatec/devise/Devise/RegistrationsController
                    new PathHttpMethod("cancel", "GET", "cancel", "(lib) devise/registrations"),
                    new PathHttpMethod("", "POST", "create", "(lib) devise/registrations"),
                    new PathHttpMethod("", "DELETE", "destroy", "(lib) devise/registrations"),
                    new PathHttpMethod("edit", "GET", "edit", "(lib) devise/registrations"),
                    new PathHttpMethod("sign_up", "GET", "new", "(lib) devise/registrations"),
                    new PathHttpMethod("", "PUT", "update", "(lib) devise/registrations")
            ));

            put("unlocks", list(
                    // :lockable module
                    // http://www.rubydoc.info/github/plataformatec/devise/Devise/UnlocksController
                    new PathHttpMethod("unlock", "POST", "create", "(lib) devise/unlocks"),
                    new PathHttpMethod("unlock/new", "GET", "new", "(lib) devise/unlocks"),
                    new PathHttpMethod("unlock", "GET", "show", "(lib) devise/unlocks")
            ));

            // Other modules are available (ie trackable, timeoutable, etc.) but HTTP endpoints could not be found for them.
        }
    };


    @Override
    public RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry routingEntry) {
        DeviseForEntry deviseForEntry = (DeviseForEntry)routingEntry;
        String baseEndpoint = deviseForEntry.baseEndpoint;

        //  Apply excluded controllers (ie 'skip:')
        if (deviseForEntry.ignoredRouteTypes != null && deviseForEntry.ignoredRouteTypes.size() > 0) {
            for (String ignoredType : deviseForEntry.ignoredRouteTypes) {
                mappedPaths.remove(ignoredType);
            }
        }

        //  Apply exclusive controllers (ie 'only:')
        if (deviseForEntry.includedRouteTypes != null && deviseForEntry.includedRouteTypes.size() > 0) {
            Set<String> keys = mappedPaths.keySet();
            for (String key : keys) {
                if (!deviseForEntry.includedRouteTypes.contains(key)) {
                    mappedPaths.remove(key);
                }
            }
        }

        // TODO - Need to figure out best way to rewrite controller + module

        //  Apply module remapping
        String moduleName = deviseForEntry.moduleName;
        if (moduleName == null) {
            moduleName = routingEntry.getModule();
        }
        if (moduleName != null) {
            for (Map.Entry<String, List<PathHttpMethod>> entry : mappedPaths.entrySet()) {
                for (PathHttpMethod method : entry.getValue()) {
                    modifyPathModule(method, moduleName);
                }
            }
        }

        //  Apply controller remapping
        if (deviseForEntry.controllerRewrites != null && deviseForEntry.controllerRewrites.size() > 0) {
            for (Map.Entry<String, String> entry : deviseForEntry.controllerRewrites.entrySet()) {
                String newController = entry.getValue();
                String oldController = entry.getKey();
                List<PathHttpMethod> paths = mappedPaths.get(oldController);
                for (PathHttpMethod path : paths) {
                    path.setControllerName(newController);
                }
            }
        }

        //  Apply endpoint remaps

        // Routes will be processed homogeneously from here
        List<PathHttpMethod> allRoutes = list();
        for (Map.Entry<String, List<PathHttpMethod>> entry : mappedPaths.entrySet()) {
            allRoutes.addAll(entry.getValue());
        }

        if (deviseForEntry.pathRewrites != null && deviseForEntry.pathRewrites.size() > 0) {
            for (Map.Entry<String, String> rewrite : deviseForEntry.pathRewrites.entrySet()) {
                for (PathHttpMethod path : allRoutes) {
                    if (path.getPath().startsWith(rewrite.getKey())) {
                        String newPath = path.getPath().substring(rewrite.getKey().length());
                        newPath = PathUtil.combine(rewrite.getValue(), newPath);
                        path.setPath(newPath);
                    } else {
                        String newPath = PathUtil.combine(baseEndpoint, path.getPath());
                        path.setPath(newPath);
                    }
                }
            }
        } else {
            for (PathHttpMethod path : allRoutes) {
                String newPath = PathUtil.combine(baseEndpoint, path.getPath());
                path.setPath(newPath);
            }
        }

        // TODO - TEST!

        ScopeEntry container = new ScopeEntry();

        //  Generate DirectHttpEntries corresponding with our routes
        for (PathHttpMethod path : allRoutes) {
            DirectHttpEntry newEntry = new DirectHttpEntry();
            newEntry.onBegin(path.getMethod());
            newEntry.onParameter(null, path.getPath(), RouteParameterValueType.STRING_LITERAL);
            newEntry.onParameter("to", path.getControllerName() + "#" + path.getAction(), RouteParameterValueType.HASH);
            newEntry.onEnd();
            container.addChildEntry(newEntry);
        }


        return container;
    }

    private void modifyPathModule(PathHttpMethod path, String newModule) {
        String currentModule = path.getControllerName();
        if (!currentModule.contains("/")) { // Doesn't contain a module identifier, must be a controller on its own
            path.setControllerName(PathUtil.combine(newModule, currentModule, false));
        } else {
            String controller = currentModule.substring(currentModule.lastIndexOf('/') + 1);
            path.setControllerName(PathUtil.combine(newModule, controller, false));
        }
    }
}
