package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsEntryShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsParameterShorthand;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsAbstractRoutingDescriptor;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.*;

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
            new PathHttpMethod("new", "GET", "new", null),
            new PathHttpMethod("", "POST", "create", null),
            new PathHttpMethod(":id", "GET", "show", null),
            new PathHttpMethod(":id/edit", "GET", "edit", null),
            new PathHttpMethod(":id", "PATCH", "update", null),
            new PathHttpMethod(":id", "PUT", "update", null),
            new PathHttpMethod(":id", "DELETE", "destroy", null)
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
            if (value.startsWith("["))
                value = value.substring(1, value.length() - 1);
            String[] concernNames = value.split(",");
            for (String concernName : concernNames) {
                concerns.add(stripColons(concernName));
            }
        } else if (name.equalsIgnoreCase("controller")) {
            controller = value;
            hasController = true;
        } else if (name.equalsIgnoreCase("path")) {
            basePath = value;
        } else if (name.equalsIgnoreCase("path_names")) {
            String[] pathChanges = value.substring(1, value.length() - 1).split(",");
            for (int i = 0; i+1 < pathChanges.length; i += 2) {
                String methodName = pathChanges[i*2 + 0];
                String newName = pathChanges[i*2 + 1];
                updateSupportedPaths(methodName, newName);
            }
        } else if (name.equalsIgnoreCase("only")) {
            List<String> allowedPaths = list();
            if (parameterType == RouteParameterValueType.ARRAY) {
                String[] paths = value.split(",");
                allowedPaths.addAll(Arrays.asList(paths));
            } else {
                allowedPaths.add(value);
            }
            for (int i = 0; i < supportedPaths.size(); i++) {
                PathHttpMethod httpPath = supportedPaths.get(i);
                if (!allowedPaths.contains(httpPath.getAction())) {
                    supportedPaths.remove(httpPath);
                    --i;
                    break;
                }
            }
        } else if (name.equalsIgnoreCase("except")) {
            List<String> removedPaths = list();
            if (parameterType == RouteParameterValueType.ARRAY) {
                String[] paths = value.split(",");
                removedPaths.addAll(Arrays.asList(paths));
            } else {
                removedPaths.add(value);
            }
            for (int i = 0; i < supportedPaths.size(); i++) {
                PathHttpMethod httpPath = supportedPaths.get(i);
                if (removedPaths.contains(httpPath.getAction())) {
                    supportedPaths.remove(httpPath);
                    --i;
                    break;
                }
            }
        }
    }

    private void updateSupportedPaths(String name, String newPath) {
        for (PathHttpMethod httpPath : supportedPaths) {
            if (httpPath.getAction().equalsIgnoreCase(name)) {
                if (!httpPath.getPath().endsWith(":id")) {
                    //  If uncontextualized, replace last path with new path
                    String[] pathParts = httpPath.getPath().split("\\/");
                    pathParts[pathParts.length - 1] = newPath;
                    StringBuilder pathBuilder = new StringBuilder();
                    for (String part : pathParts) {
                        if (pathBuilder.length() > 0)
                            pathBuilder.append('/');
                        pathBuilder.append(part);
                    }
                    httpPath.setPath(pathBuilder.toString());
                } else {
                    //  If contextualized to an instance, just append to end of URL
                    httpPath.setPath(PathUtil.combine(httpPath.getPath(), newPath));
                }
            }
        }
    }

    @Override
    public String getControllerName() {
        return getParentControllerIfNull(controller);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(basePath);
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        List<PathHttpMethod> result = list();
        for (PathHttpMethod method : supportedPaths) {
            String subPath = method.getPath();
            subPath = makeRelativeSubPath(subPath);
            result.add(new PathHttpMethod(subPath, method.getMethod(), method.getAction(), getControllerName()));
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
