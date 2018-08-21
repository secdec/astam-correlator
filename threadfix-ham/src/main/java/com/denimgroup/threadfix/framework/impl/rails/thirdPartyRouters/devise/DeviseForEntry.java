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

package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise;

import com.denimgroup.threadfix.framework.impl.rails.model.*;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

//  See: http://www.rubydoc.info/github/plataformatec/devise/master/ActionDispatch/Routing/Mapper#devise_for-instance_method

//  This handles route creation for ALL devise module types. This should be split out into separate entries
//  for devise_password, devise_registration, etc. and deferred to from here.
public class DeviseForEntry extends AbstractRailsRoutingEntry {

    String baseEndpoint = null; // Parameter 'path'
    String moduleName = null; // Parameter 'module', refers to devise modules by default
    List<String> ignoredRouteTypes = list(); // User specifically ignores a route using 'skip:'
    List<String> includedRouteTypes = list(); // User specifically ignores all except some routes using 'only:'
    Map<String, String> pathRewrites = map(); // Parameter 'path_names'
    Map<String, String> controllerRewrites = map(); // Parameter 'controllers'




    @Override
    public void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, nameType, value, parameterType);

        if (name == null) {
            baseEndpoint = value;
        } else if (name.equalsIgnoreCase("path")) {
            baseEndpoint = value;
        } else if (name.equalsIgnoreCase("path_names")) {
            pathRewrites.put(name, value);
        } else if (name.equalsIgnoreCase("controllers")) {
            if (parameterType == RouteParameterValueType.HASH) {
                Map<String, String> map = parseHashString(value);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    controllerRewrites.put(entry.getKey(), entry.getValue());
                }
            } else {
                controllerRewrites.put(name, value);
            }
        } else if (name.equalsIgnoreCase("module")) {
            moduleName = value;
        }
    }

    @Override
    public String getModule() {
        if (moduleName != null) {
            return moduleName;
        } else {
            return getParentModule();
        }
    }

    @Override
    public String getPrimaryPath() {
        return baseEndpoint;
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentController();
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        DeviseForEntry clone = new DeviseForEntry();
        clone.moduleName = moduleName;
        clone.controllerRewrites = new HashMap<String, String>(controllerRewrites);
        clone.pathRewrites = new HashMap<String, String>(pathRewrites);
        clone.baseEndpoint = baseEndpoint;
        clone.ignoredRouteTypes = new ArrayList<String>(ignoredRouteTypes);
        clone.includedRouteTypes = new ArrayList<String>(includedRouteTypes);
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return list((RouteShorthand)new DeviseForShorthand());
    }
}
