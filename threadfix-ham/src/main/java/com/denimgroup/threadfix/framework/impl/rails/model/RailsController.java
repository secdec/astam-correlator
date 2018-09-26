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
package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.enums.ParameterDataType;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

/**
 * Created by sgerick on 4/30/2015.
 */
public class RailsController {
    private File controllerFile;
    private String controllerName;
    private String controllerField;
    private String moduleName;
    private List<RailsControllerMethod> controllerMethods;


    public File getControllerFile() {
        return controllerFile;
    }

    public void setControllerFile(File controllerFile) {
        this.controllerFile = controllerFile;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
        this.controllerField = controllerName;
        this.controllerField = this.controllerField.replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase();

    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        if (moduleName != null) {
            this.moduleName = moduleName.replace("::", "/");
        }
    }

    public String getControllerField() {
        return controllerField;
    }

    public List<RailsControllerMethod> getControllerMethods() {
        return controllerMethods;
    }

    public void addControllerMethod(RailsControllerMethod controllerMethod) {
        if (this.controllerMethods == null)
            this.controllerMethods = list();
        this.controllerMethods.add(controllerMethod);
    }

    public RailsControllerMethod getMethod(String name) {
    	if (controllerMethods == null) {
    		return null;
	    }

    	for (RailsControllerMethod method : controllerMethods) {
    		if (method.getMethodName().equals(name)) {
    			return method;
		    }
	    }

	    return null;
    }

    public Map<String, RouteParameter> getParameters() {
        Map<String, RouteParameter> p = map();
        for (RailsControllerMethod rcm : controllerMethods) {
            if (rcm.getMethodParams() != null)
                p.putAll(rcm.getMethodParams());
        }
        return p;
    }

	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	if (moduleName != null) {
    		sb.append(moduleName);
	    }
	    sb.append("/");
    	if (controllerName != null) {
    		sb.append(controllerName);
	    } else {
    		sb.append("[null-controller]");
	    }
	    sb.append(" (");
    	if (controllerMethods != null) {
    		sb.append(controllerMethods.size());
	    } else {
    		sb.append("no");
	    }
	    sb.append(" methods)");
		return sb.toString();
	}
}
