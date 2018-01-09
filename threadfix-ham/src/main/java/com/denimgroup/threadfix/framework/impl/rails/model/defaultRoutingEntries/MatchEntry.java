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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

// http://guides.rubyonrails.org/routing.html#http-verb-constraints

//  Generic routing syntax for responding to multiple HTTP query types with a single routing entry
public class MatchEntry extends AbstractRailsRoutingEntry {

    String endpoint = null;
    String controller = null;
    String moduleName = null;
    String actionName = null;
    boolean anchor = true;
    List<String> httpMethods = list("GET");

    @Override
    public void onParameter(String name, String value, RouteParameterValueType parameterType) {
        if (name == null) {
            endpoint = value;
        } else if (name.equalsIgnoreCase("to")) {
            String[] controllerParts = value.split("#");
            if (controllerParts.length == 1) {
                actionName = controllerParts[0];
            } else if (controllerParts.length == 2) {
                controller = controllerParts[0];
                actionName = controllerParts[1];
            }
        } else if (name.equalsIgnoreCase("via")) {
            httpMethods.clear();
            // Strip brackets
            if (parameterType == RouteParameterValueType.ARRAY) {
                value = value.substring(1, value.length() - 1);
            }
            String[] methods = value.split(",");
            if (methods.length == 1) {
                if (stripColons(methods[0]).equalsIgnoreCase("all")) {
                    methods = new String[] { "get", "post", "put", "patch", "delete" };
                }
            }

            for (String method : methods) {
                httpMethods.add(stripColons(method).toUpperCase());
            }
        } else if (name.equalsIgnoreCase("controller")) {
            controller = value;
        } else if (name.equalsIgnoreCase("action")) {
            actionName = value;
        } else if (name.equalsIgnoreCase("module")) {
            moduleName = value;
        } else if (name.equalsIgnoreCase("anchor")) {
            anchor = value.equalsIgnoreCase("true");
        } else if (endpoint == null && controller == null && actionName == null) {
            //  Must be an initial parameter of ie '/path' => 'controller#action'
            endpoint = name;
            controller = extractController(value);
            actionName = extractAction(value);
        }
    }

    @Override
    public String getPrimaryPath() {
        return makeRelativePathToParent(endpoint);
    }

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
        MatchEntry clone = new MatchEntry();
        clone.endpoint = endpoint;
        clone.controller = controller;
        clone.actionName = actionName;
        clone.httpMethods.addAll(httpMethods);
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        List<PathHttpMethod> result = list();
        String trueEndpoint = endpoint;
        if (!anchor) {
            trueEndpoint += "(:any)";
        }
        for (String method : httpMethods) {
            result.add(new PathHttpMethod(trueEndpoint, method, actionName, controller));
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
        result.append("#<> ");
        result.append("via: [");
        for (String method : httpMethods) {
            result.append(method);
            result.append(",");
        }
        result.append("]");
        return result.toString();
    }
}
