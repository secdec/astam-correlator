////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class WildcardEndpointPathNode implements EndpointPathNode {

    private static Pattern namedGroupPattern = Pattern.compile("\\(\\?P\\<\\w+\\>");

    private Pattern wildcardPattern;

    public WildcardEndpointPathNode(String pattern) {
    	if (pattern == null) {
    		this.wildcardPattern = Pattern.compile(".*");
	    } else {
    	    pattern = namedGroupPattern.matcher(pattern).replaceAll("(");
		    this.wildcardPattern = Pattern.compile(pattern);
	    }
    }

    @Override
    public boolean matches(@Nonnull String pathPart) {
        return wildcardPattern.matcher(pathPart).matches();
    }

    @Override
    public boolean matches(@Nonnull EndpointPathNode node) {
        return node instanceof WildcardEndpointPathNode && ((WildcardEndpointPathNode) node).wildcardPattern.equals(this.wildcardPattern);
    }

    @Override
    public String toString() {
        return "wildcard";
    }
}
