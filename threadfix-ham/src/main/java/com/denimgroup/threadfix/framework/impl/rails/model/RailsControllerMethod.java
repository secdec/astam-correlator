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
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.rails.model;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.map;

/**
* Created by sgerick on 4/30/2015.
*/
public class RailsControllerMethod {
    private String methodName;
    private Map<String, ParameterDataType> methodParams;
    private int startLine, endLine;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, ParameterDataType> getMethodParams() {
        return methodParams;
    }

    public void addMethodParam(String methodParam, ParameterDataType dataType) {
        if (methodParam == null) return;

        if (this.methodParams == null)
            this.methodParams = map();
        this.methodParams.put(methodParam, dataType);
    }

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}
}
