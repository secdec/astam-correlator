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

package com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRouter;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseForEntry;
import com.denimgroup.threadfix.framework.impl.rails.thirdPartyRouters.devise.DeviseScopeEntry;

//  TODO - Check 'config/initializers/devise.rb' to determine loaded support modules
public class DeviseRouter implements RailsRouter {
    @Override
    public RailsRoutingEntry identify(String identifier) {
        if (identifier.equalsIgnoreCase("devise_for")) {
            return new DeviseForEntry();
        } else if (identifier.equalsIgnoreCase("devise_scope") || identifier.equalsIgnoreCase("as")) {
            //  'as' is an alias for 'devise_scope':
            //      http://rubydoc.info/github/plataformatec/devise/master/ActionDispatch/Routing/Mapper#devise_scope-instance_method
            return new DeviseScopeEntry();
        } else {
            return null;
        }
    }

    @Override
    public String resolveController(String controllerPath) {
        if (controllerPath != null && controllerPath.startsWith("devise/")) {
            // Denote with '(lib)' to indicate that the controller
            //  is in a third-party library and the file can't be resolved
            controllerPath = "(lib) " + controllerPath;
        }
        return controllerPath;
    }
}
