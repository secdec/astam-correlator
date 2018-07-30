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

package com.denimgroup.threadfix.framework.impl.rails.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public interface RailsRoutingEntry {

    void addChildEntry(RailsRoutingEntry child);
    void removeChildEntry(RailsRoutingEntry child);
    List<RailsRoutingEntry> getChildren();
    void setParent(RailsRoutingEntry parent);
    RailsRoutingEntry getParent();
    void setLineNumber(int codeLine);
    int getLineNumber();

    void onToken(int type, int lineNumber, String stringValue);
    void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType);
    void onInitializerParameter(String name, String value, RouteParameterValueType parameterType);

    void onBegin(String identifier);
    void onEnd();

    /**
     * @return The main path for this entry that will host its endpoints and be the base endpoint for its children.
     */
    String getPrimaryPath();


    /**
     * @return A set of complete endpoints generated directly by this routing entry. The PathHttpMethod
     * should have a URL, HTTP query method, action name, and controller name attached.
     */
    Collection<PathHttpMethod> getPaths();


    /**
     * @return The name of the controller directly assigned to this entry or the name of the closest available controller
     * in this entry's parents.
     */
    String getControllerName();


    /**
     * @return The name of the Ruby module directly assigned to this entry or the name of the closest available module
     * name in this entry's parents.
     */
    String getModule();

    /**
     * @return A set of RouteShorthand implementations that can be used when parsing this route entry.
     */
    Collection<RouteShorthand> getSupportedShorthands();

    /**
     * @return Generates a deep clone of the current entry, duplicating child entries while the clone shares the original parent.
     */
    @Nonnull
    RailsRoutingEntry cloneEntry();
}
