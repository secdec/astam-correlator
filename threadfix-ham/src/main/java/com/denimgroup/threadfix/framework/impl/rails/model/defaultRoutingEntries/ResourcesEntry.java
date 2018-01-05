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
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ManyResourcesShorthand;
import com.denimgroup.threadfix.framework.util.PathUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#crud-verbs-and-actions
// https://stackoverflow.com/questions/11356146/difference-between-resource-and-resources-in-rails-routing

//  A shorthand for defining a set of RESTful routes pertaining to a collection of
//  data instances such as all users.
public class ResourcesEntry extends AbstractRailsRoutingEntry implements Concernable {

    String dataSourceSymbol;
    String basePath;
    String controllerName = null;
    List<String> concerns = list();

    List<PathHttpMethod> supportedPaths = list(
            new PathHttpMethod("", "GET", "index", null),
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
        if (name == null) {
            //  May be a shorthand declaring multiple resource routes at once, if so simply append the
            //      names and separate with a space and the MultiResourcesShorthand will expand into
            //      individual resource declarations
            if (dataSourceSymbol != null) {
                dataSourceSymbol += " " + value;
            } else {
                dataSourceSymbol = value;
            }

            if (basePath != null) {
                basePath += " " + value;
            } else {
                basePath = value;
            }

            String parsedControllerName = value.split("\\/")[0];

            if (controllerName != null) {
                controllerName += " " + parsedControllerName;
            } else {
                controllerName = parsedControllerName;
            }
        } else if (name.equalsIgnoreCase("concerns")) {
            //  Strip brackets on either side
            if (value.startsWith("["))
                value = value.substring(1, value.length() - 1);
            String[] valueParts = value.split(",");
            for (String concern : valueParts) {
                concerns.add(stripColons(concern));
            }
        } else if (name.equalsIgnoreCase("controller")) {
            controllerName = value;
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
        return getParentControllerIfNull(controllerName);
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
        for (PathHttpMethod path : supportedPaths) {
            String fullPath = makeRelativeSubPath(path.getPath());
            result.add(new PathHttpMethod(fullPath, path.getMethod(), path.getAction(), getControllerName()));
        }
        return result;
    }



    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("resources :");
        result.append(dataSourceSymbol);
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
        return null;
    }

    @Override
    public void resetConcerns() {
        concerns.clear();
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return list(new ConcernsParameterShorthand(), new ManyResourcesShorthand());
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ResourcesEntry clone = new ResourcesEntry();
        clone.concerns.addAll(concerns);
        clone.supportedPaths.addAll(supportedPaths);
        clone.basePath = basePath;
        clone.dataSourceSymbol = dataSourceSymbol;
        cloneChildrenInto(clone);
        return clone;
    }
}
