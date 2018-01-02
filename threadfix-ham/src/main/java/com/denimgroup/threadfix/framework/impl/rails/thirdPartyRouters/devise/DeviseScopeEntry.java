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

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import javax.annotation.Nonnull;
import java.util.Collection;

//  See: http://www.rubydoc.info/github/plataformatec/devise/master/ActionDispatch/Routing/Mapper#devise_scope-instance_method

//  Doesn't actually do anything - this is a detail regarding which model type to use, which isn't relevant for
//  us at the moment.
public class DeviseScopeEntry extends AbstractRailsRoutingEntry {

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public String getPrimaryPath() {
        return null;
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
        return null;
    }
}
