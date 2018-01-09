package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#generating-paths-and-urls-from-code


//  Route entries 'get', 'post', 'put', 'patch', 'delete'
public class DirectHttpEntry extends AbstractRailsRoutingEntry {

    String mappedEndpoint = null;
    String controller = null;
    String actionName = null;
    String httpMethod = null;
    Map<String, String> defaults = new HashMap<String, String>();

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controller);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }


    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        DirectHttpEntry clone = new DirectHttpEntry();
        clone.mappedEndpoint = mappedEndpoint;
        clone.controller = controller;
        clone.actionName = actionName;
        clone.httpMethod = httpMethod;
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        String relativeEndpoint = makeRelativePathToParent(mappedEndpoint);
        PathHttpMethod result = new PathHttpMethod(relativeEndpoint, httpMethod, actionName, controller);
        return list(result);
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        value = stripColons(value);
        if (name == null) {
            mappedEndpoint = value;
        } else if (name.equalsIgnoreCase("to")) {
            String[] controllerParts = value.split("#");
            if (controllerParts.length == 1) {
                actionName = controllerParts[0];
            } else if (controllerParts.length == 2) {
                controller = controllerParts[0];
                actionName = controllerParts[1];
            }
        } else if (name.equalsIgnoreCase("controller")) {
            controller = value;
        } else if (name.equalsIgnoreCase("constraints")) {
            //  Ignore for now
        } else if (name.equalsIgnoreCase("defaults")) {
            String[] values = value.substring(1, value.length() - 1).split(",");
            for (int i = 0; i < values.length; i++) {
                String[] valueParts = values[i].split(":");
                String defaultName = valueParts[i];
                String defaultValue = valueParts[i + 1];
                defaults.put(defaultName, defaultValue);
            }
        } else if (name.equalsIgnoreCase("action")) {
            actionName = value;
        } else {
            if (mappedEndpoint == null) {
                //  Assume syntax '/endpoint' => 'controller#method' or '/endpoint' => 'method'
                mappedEndpoint = name;
                String[] controllerParts = value.split("#");
                if (controllerParts.length == 1) {
                    actionName = controllerParts[0];
                } else if (controllerParts.length == 2) {
                    controller = controllerParts[0];
                    actionName = controllerParts[1];
                }
            }
        }
    }

    @Override
    public void onBegin(String identifier) {
        httpMethod = identifier.toUpperCase();
    }

    @Override
    public void onEnd() {
        if (controller == null && actionName == null) {
            String[] pathParts = mappedEndpoint.split("\\/");
            controller = pathParts[0];
            if (pathParts.length > 1)
                actionName = pathParts[1];
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(mappedEndpoint);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(httpMethod);
        result.append(" ");
        result.append(mappedEndpoint);
        if (controller != null && actionName != null) {
            result.append(", to: ");
            result.append(controller);
            result.append("#");
            result.append(actionName);
        }
        return result.toString();
    }
}
