////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
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
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////


package com.denimgroup.threadfix.framework.impl.dotNet;

import com.denimgroup.threadfix.data.entities.ModelField;
import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpClass;
import com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions.CSharpMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.framework.impl.dotNet.Action.action;

/**
 * Created by mac on 6/11/14.
 */
public class DotNetControllerMappings {
    private String       areaName = null;
    private String       controllerName = null;
    private CSharpClass  controllerClass = null;
    private List<Action> actions        = list();
    private String       namespace = null;
    private List<String> explicitRoutes = list();

    public String getFilePath() {
        return filePath;
    }

    private final String filePath;

    public void setControllerName(@Nonnull String controllerName) {
        assert this.controllerName == null : "These mappings already have a controller name.";
        this.controllerName = controllerName;
    }

    public String getControllerName() {
        assert controllerName != null : "You have attempted to access the controller name without setting it.";
        return controllerName;
    }

    public void setControllerClass(CSharpClass controllerClass) {
        this.controllerClass = controllerClass;
    }

    public CSharpClass getControllerClass() {
        return controllerClass;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setAreaName(@Nonnull String areaName) {
        assert this.areaName == null : "These mappings already have an area name.";
        this.areaName = areaName;
    }

    public String getAreaName() {
        assert areaName != null : "You have attempted to access the area name without setting it.";
        return areaName;
    }

    public boolean hasActionNames() {
        //  Whether or not actions from this controller are to be mapped by their name
        for (Action action : actions) {
            if (action.isMethodBasedAction) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAreaName(){ return areaName != null && !areaName.isEmpty();}

    public boolean hasValidMappings() {
        return controllerName != null && !actions.isEmpty();
    }

    // TODO refactor, this is a lot of parameters
    public void addAction(@Nonnull String action,
                          @Nonnull Set<String> attributes,
                          @Nonnull Integer lineNumber,
                          @Nonnull Integer endLineNumber,
                          @Nonnull Set<RouteParameter> parametersWithTypes,
                          @Nullable String explicitRoute,
                          @Nonnull CSharpMethod actionMethod,
                          boolean isMethodBasedAction) {
        actions.add(action(action, attributes, lineNumber, endLineNumber, parametersWithTypes, explicitRoute, actionMethod, isMethodBasedAction));
    }

    @Nonnull
    public List<Action> getActions() {
        return actions;
    }

    @Nullable
    public Action getActionForNameAndMethod(String actionName, String method) {
        String allCapsMethod = method.toUpperCase();

        for (Action action : actions) {
            if (action.name.equalsIgnoreCase(actionName) && (action.getMethods().contains(allCapsMethod) || action.name.equalsIgnoreCase(method))) {
                return action;
            }
        }

        //  No directly-matching action found; if method is GET, match against first non-decorated action with the given name
        //  since HTTP method is implicitly GET
        if (method.equals("GET")) {
            for (Action action : actions) {
                if (action.name.equalsIgnoreCase(actionName) && action.getMethods().isEmpty()) {
                    return action;
                }
            }
        }

        return null;
    }

    @Nullable
    public Action getActionForLines(int startLine, int endLine) {
        for (Action action : actions) {
            if (action.lineNumber == startLine && action.endLineNumber == endLine) {
                return action;
            }
        }
        return null;
    }

    public void addExplicitRoute(String explicitRoute) {
        explicitRoutes.add(explicitRoute);
    }

    public List<String> getExplicitRoutes() {
        return explicitRoutes;
    }

    public DotNetControllerMappings(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "DotNetControllerMappings{" +
                ((areaName != null && !areaName.isEmpty()) ? "areaName='" + areaName + '\''  : "") +
                "controllerName='" + controllerName + '\'' +
                ", actions=" + actions +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
