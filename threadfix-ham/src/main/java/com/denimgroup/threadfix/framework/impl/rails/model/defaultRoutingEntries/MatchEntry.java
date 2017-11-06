package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries;

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteParameterValueType;
import com.denimgroup.threadfix.framework.util.PathUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#http-verb-constraints

//  Generic routing syntax for responding to multiple HTTP query types with a single routing entry
public class MatchEntry extends AbstractRailsRoutingEntry {

    String endpoint = null;
    String controller = null;
    String methodName = null;
    Collection<String> httpMethods = list();

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("to")) {
            String[] controllerParts = value.split("#");
            controller = controllerParts[0];
            methodName = controllerParts[1];
        } else if (name.equalsIgnoreCase("via")) {
            // Strip brackets
            value = value.substring(1, value.length() - 1);
            String[] methods = value.split(",");
            for (String method : methods) {
                httpMethods.add(stripColons(method).toUpperCase());
            }
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(endpoint);
    }

    @Override
    public String getControllerName() {
        return controller;
    }

    @Override
    public String getActionMethodName() {
        return methodName;
    }

    @Override
    public RailsRoutingEntry cloneEntry() {
        MatchEntry clone = new MatchEntry();
        clone.endpoint = endpoint;
        clone.controller = controller;
        clone.methodName = methodName;
        clone.httpMethods.addAll(httpMethods);
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<PathHttpMethod> getSubPaths() {
        List<PathHttpMethod> result = list();
        for (String method : httpMethods) {
            result.add(new PathHttpMethod(endpoint, method));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("match ");
        result.append('\'');
        result.append(endpoint);
        result.append('\'');
        result.append(" to: ");
        result.append(getControllerName());
        result.append("#");
        result.append(getActionMethodName());
        result.append("via: [");
        for (String method : httpMethods) {
            result.append(method);
            result.append(",");
        }
        result.append("]");
        return result.toString();
    }
}
