package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsEntryShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsParameterShorthand;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#singular-resources
// https://stackoverflow.com/questions/11356146/difference-between-resource-and-resources-in-rails-routing

//  A shorthand for defining a set of RESTful routes pertaining to a singular
//  data instance such as the current logged-in user.
public class ResourceEntry extends AbstractRailsRoutingEntry implements Concernable {

    boolean hasController = false;
    String controller;
    String basePath = null;
    List<String> concerns = list();
    List<PathHttpMethod> supportedPaths = list(
            new PathHttpMethod("new", "GET", "new"),
            new PathHttpMethod("", "POST", "create"),
            new PathHttpMethod(":id", "GET", "show"),
            new PathHttpMethod(":id/edit", "GET", "edit"),
            new PathHttpMethod(":id", "PATCH", "update"),
            new PathHttpMethod(":id", "PUT", "update"),
            new PathHttpMethod(":id", "DELETE", "destroy")
            );

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (!hasController) {
            if (parameterType == RouteParameterValueType.SYMBOL) {
                controller = value;
            }
            hasController = true;
            basePath = value;
        } else if (name.equalsIgnoreCase("path")) {
            basePath = value;
        } else if (name.equalsIgnoreCase("concerns")) {
            //  Strip brackets on each side
            value = value.substring(1, value.length() - 1);
            String[] concernNames = value.split(",");
            for (String concernName : concernNames) {
                concerns.add(stripColons(concernName));
            }
        }
    }

    @Override
    public String getControllerName() {
        return controller;
    }

    @Override
    public String getActionMethodName() {
        return null;
    }

    @Override
    public void setControllerName(String controllerName) {

    }

    @Override
    public void setActionMethodName(String actionMethodName) {

    }

    @Override
    public void onBegin(String identifier) {
    }

    @Override
    public void onEnd() {

    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(basePath);
    }

    @Override
    public Collection<PathHttpMethod> getSubPaths() {
        List<PathHttpMethod> result = list();
        for (PathHttpMethod method : supportedPaths) {
            String subPath = method.getPath();
            subPath = makeRelativeSubPath(subPath);
            result.add(new PathHttpMethod(subPath, method.getMethod(), method.getAction()));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("resource :");
        result.append(basePath);
        return result.toString();
    }

    private String makeRelativeSubPath(String subPath) {
        String finalPath = makeRelativePathToParent(basePath);
        if (subPath.length() > 0) {
            finalPath = PathUtil.combine(finalPath, subPath);
        }
        return finalPath;
    }

    @Override
    public Collection<String> getConcerns() {
        return concerns;
    }

    @Override
    public void resetConcerns() {
        concerns.clear();
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return list((RouteShorthand)new ConcernsParameterShorthand());
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        ResourceEntry clone = new ResourceEntry();
        clone.concerns.addAll(concerns);
        clone.supportedPaths.addAll(supportedPaths);
        clone.basePath = basePath;
        clone.hasController = hasController;
        clone.controller = controller;
        cloneChildrenInto(clone);
        return clone;
    }
}
