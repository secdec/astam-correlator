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

package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsParameterShorthand;
import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.PathUtil;

import javax.annotation.Nonnull;
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
            new PathHttpMethod("", "GET", "show", null),
            new PathHttpMethod("edit", "GET", "edit", null),
            new PathHttpMethod("", "PATCH", "update", null),
            new PathHttpMethod("", "PUT", "update", null),
            new PathHttpMethod("", "DELETE", "destroy", null)
            );

    @Override
    public void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType) {
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
                concerns.add(CodeParseUtil.trim(concernName, ":"));
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
            CodeParseUtil.trim(allowedPaths, ":");
            for (int i = 0; i < supportedPaths.size(); i++) {
                PathHttpMethod httpPath = supportedPaths.get(i);
                if (!allowedPaths.contains(httpPath.getAction())) {
                    supportedPaths.remove(httpPath);
                    --i;
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
            CodeParseUtil.trim(removedPaths, ":");
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

    @Nonnull
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
