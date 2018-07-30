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

public class ControllerEntry extends AbstractRailsRoutingEntry {
    String controllerName;

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
        return getParentControllerIfNull(controllerName);
    }

    @Override
    public String getModule() {
        return getParentModule();
    }

    @Override
    public void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType) {
        super.onParameter(name, nameType, value, parameterType);
        controllerName = value;
    }


    @Nonnull
    @Override
    public RailsRoutingEntry cloneEntry() {
        ControllerEntry clone = new ControllerEntry();
        clone.controllerName = controllerName;
        cloneChildrenInto(clone);
        return clone;
    }
}
