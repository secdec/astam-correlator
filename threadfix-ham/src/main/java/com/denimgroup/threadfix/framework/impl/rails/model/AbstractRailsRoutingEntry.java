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

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.PathUtil;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class AbstractRailsRoutingEntry implements RailsRoutingEntry {

    RailsRoutingEntry parentEntry;
    List<RailsRoutingEntry> children = list();
    int lineNumber = -1;


    @Override
    public void onBegin(String identifier) {

    }

    @Override
    public void onParameter(String name, RouteParameterValueType nameType, String value, RouteParameterValueType parameterType) {

    }

    @Override
    public void onInitializerParameter(String name, String value, RouteParameterValueType parameterType) {

    }

    @Override
    public void onToken(int type, int lineNumber, String stringValue) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public boolean canGenerateEndpoints() {
        return parentEntry == null || parentEntry.canGenerateEndpoints();
    }

    @Override
    public void setLineNumber(int codeLine) {
        lineNumber = codeLine;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void addChildEntry(RailsRoutingEntry child) {
        if (!children.contains(child)) {
            children.add(child);
        }
        child.setParent(this);
    }

    @Override
    public void removeChildEntry(RailsRoutingEntry child) {
        if (children.contains(child)) {
            children.remove(child);
        }
        child.setParent(null);
    }

    @Override
    public List<RailsRoutingEntry> getChildren() {
        return children;
    }

    @Override
    public void setParent(RailsRoutingEntry parent) {
        if (this.parentEntry == parent) {
            return;
        }

        if (this.parentEntry != null && this.parentEntry.getChildren().contains(this)) {
            this.parentEntry.removeChildEntry(this);
        }
        parentEntry = parent;
    }

    @Override
    public RailsRoutingEntry getParent() {
        return parentEntry;
    }

    protected String cleanCodeString(String codeString) {
        codeString = codeString.trim();
        codeString = CodeParseUtil.trim(codeString, ":");
        while (codeString.startsWith("{") || codeString.startsWith("'") || codeString.startsWith("\"")) {
            codeString = codeString.substring(1);
        }

        while (codeString.endsWith("}") || codeString.endsWith("'") || codeString.endsWith("\"")) {
            codeString = codeString.substring(0, codeString.length() - 1);
        }
        return codeString;
    }

    /**
     * @param controllerDescriptor A string of the format 'controller#action' or 'controller'
     */
    protected String extractController(String controllerDescriptor) {
        if (!controllerDescriptor.contains("#")) {
            return controllerDescriptor;
        } else {
            return controllerDescriptor.split("#")[0];
        }
    }

    /**
     * @param actionDescriptor A string of the format 'controller#action' or 'action'
     */
    protected String extractAction(String actionDescriptor) {
        if (!actionDescriptor.contains("#")) {
            return actionDescriptor;
        } else {
            return actionDescriptor.split("#")[1];
        }
    }

    protected String makeRelativePathToParent(String path) {
        String parentPath;
        if (getParent() != null) {
            parentPath = getParent().getPrimaryPath();
        } else {
            parentPath = "/";
        }
        return PathUtil.combine(parentPath, path);
    }

    protected void cloneChildrenInto(RailsRoutingEntry target) {
        for (RailsRoutingEntry entry : children) {
            target.addChildEntry(entry.cloneEntry());
        }
    }

    protected final String getParentControllerIfNull(String controllerName) {
        if (controllerName != null) {
            return controllerName;
        } else {
            return getParentController();
        }
    }

    protected final String getParentController() {
        String controllerName = null;
        RailsRoutingEntry currentEntry = getParent();
        while (currentEntry != null) {
            controllerName = currentEntry.getControllerName();
            if (controllerName != null)
                break;
            currentEntry = currentEntry.getParent();
        }
        return controllerName;
    }

    protected final String getParentModuleIfNull(String moduleName) {
        if (moduleName != null) {
            return moduleName;
        } else {
            return getParentModule();
        }
    }

    protected final String getParentModule() {
        String moduleName = null;
        RailsRoutingEntry currentEntry = getParent();
        while (currentEntry != null) {
            moduleName = currentEntry.getModule();
            if (moduleName != null)
                break;
            currentEntry = currentEntry.getParent();
        }
        return moduleName;
    }

    protected Map<String, String> parseHashString(String hashString) {
        Map<String, String> result = new HashMap<String, String>();
        hashString = hashString.substring(1, hashString.length() - 1);
        String[] entries;
        entries = hashString.split(",");
        for (String entry : entries) {
            String[] parts;
            if (entry.contains("=>")) {
                parts = entry.split("=>");
            } else {
                parts = entry.split("\\:");
            }
            String key = cleanCodeString(parts[0]);
            String value = cleanCodeString(parts[1]);
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Collection<RouteShorthand> getSupportedShorthands() {
        return null;
    }
}
