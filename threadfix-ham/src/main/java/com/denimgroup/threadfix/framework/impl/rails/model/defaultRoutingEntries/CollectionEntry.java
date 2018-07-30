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

// http://guides.rubyonrails.org/routing.html#adding-collection-routes
// https://stackoverflow.com/questions/3028653/difference-between-collection-route-and-member-route-in-ruby-on-rails

//  Collection routes are used within a 'resources' route to add more routes
//  to the pre-existing ones defined by 'resources'. Collection routes
//  are relative to collection endpoints and are not added to instance endpoints.
public class CollectionEntry extends AbstractRailsRoutingEntry {

    @Override
    public String getPrimaryPath() {
        return getParent() == null ? null : getParent().getPrimaryPath();
    }

    @Override
    public Collection<PathHttpMethod> getPaths() {
        return null;
    }

    @Override
    public String getControllerName() {
        return getParentController();
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("collection (");
        result.append(this.getChildren().size());
        result.append(" subroutes)");
        return result.toString();
    }

    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        CollectionEntry clone = new CollectionEntry();
        cloneChildrenInto(clone);
        return clone;
    }
}
