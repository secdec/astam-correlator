package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.util.PathUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#generating-paths-and-urls-from-code


//  Route entries 'get', 'post', 'put', 'patch', 'delete'
public class DirectHttpEntry extends AbstractRailsRoutingEntry {

    String mappedEndpoint = null;
    String controller = null;
    String actionName = null;
    String httpMethod = null;

    @Override
    public String getControllerName() {
        return controller;
    }

    @Override
    public String getActionMethodName() {
        return actionName;
    }

    @Override
    public void setControllerName(String controllerName) {
        controller = controllerName;
    }

    @Override
    public void setActionMethodName(String actionMethodName) {
        actionName = actionMethodName;
    }

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
    public Collection<PathHttpMethod> getSubPaths() {
        return null;
    }

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        value = stripColons(value);
        if (name == null) {
            mappedEndpoint = value;
        } else if (name.equalsIgnoreCase("to")) {
            String[] controllerParts = value.split("#");
            controller = controllerParts[0];
            actionName = controllerParts[1];
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
