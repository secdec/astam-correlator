package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.*;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsEntryShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsParameterShorthand;
import com.denimgroup.threadfix.framework.util.PathUtil;

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
            new PathHttpMethod("", "GET", "index"),
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
        if (name == null) {
            dataSourceSymbol = value;
            basePath = value;
            controllerName = value.split("\\/")[0];
        } else if (name.equalsIgnoreCase("concerns")) {
            //  Strip braces on either side
            value = value.substring(1, value.length() - 1);
            String[] valueParts = value.split(",");
            for (String concern : valueParts) {
                concerns.add(stripColons(concern));
            }
        }
    }

    @Override
    public String getControllerName() {
        return controllerName;
    }

    @Override
    public String getActionMethodName() {
        return null;
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
        for (PathHttpMethod path : supportedPaths) {
            String fullPath = makeRelativeSubPath(path.getPath());
            result.add(new PathHttpMethod(fullPath, path.getMethod(), path.getAction()));
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
        return list((RouteShorthand)new ConcernsParameterShorthand());
    }

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
