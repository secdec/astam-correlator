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

import com.denimgroup.threadfix.framework.impl.rails.model.AbstractRailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.PathHttpMethod;
import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands.ConcernsEntryShorthand;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.denimgroup.threadfix.CollectionUtils.list;


//  A shorthand for multiple 'concern' declarations
public class ConcernsEntry extends AbstractRailsRoutingEntry implements Concernable {

    Collection<String> concernIds = list();

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

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("concerns [");
        if (concernIds != null) {
            for (String concern : concernIds) {
                result.append(":");
                result.append(concern);
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return list((RouteShorthand)new ConcernsEntryShorthand());
    }


    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ConcernsEntry clone = new ConcernsEntry();
        clone.concernIds.addAll(concernIds);
        cloneChildrenInto(clone);
        return clone;
    }

    @Override
    public Collection<String> getConcerns() {
        return concernIds;
    }

    @Override
    public void resetConcerns() {

    }
}
