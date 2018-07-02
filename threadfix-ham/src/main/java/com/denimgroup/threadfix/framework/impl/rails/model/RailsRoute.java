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

import java.util.ArrayList;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Created by sgerick on 5/5/2015.
 */
public class RailsRoute {
    private String url;
    private String httpMethod;
    private String controller;
    private String controllerMethod;

    public RailsRoute() {
    }

    public RailsRoute(String url, String method) {
        this.setUrl(url);
        this.httpMethod = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

	public void setController(String controller, String controllerMethod) {
		this.controller = controller;
		this.controllerMethod = controllerMethod;
	}

    public String getController() {
        return controller;
    }

	public String getControllerMethod() {
		return controllerMethod;
	}

	@Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	if (url != null) {
		    sb.append(url);
	    } else {
    		sb.append("[null-url]");
	    }
    	sb.append(" (");
    	if (controller != null) {
    		sb.append(controller);
	    } else {
    		sb.append("[null-controller]");
	    }
	    sb.append("::");
    	if (controllerMethod != null) {
    		sb.append(controllerMethod);
	    } else {
    		sb.append("[null-method]");
	    }
	    sb.append(")");
        return sb.toString();
    }
}
