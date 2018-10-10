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

import java.util.ArrayList;
import java.util.List;

public class EndpointStructureNode {

    private EndpointPathNode pathNode;
    private List<EndpointStructureNode> children;

    public EndpointStructureNode(EndpointPathNode pathNode) {
        this.pathNode = pathNode;
        this.children = new ArrayList<>();
    }

    public EndpointStructureNode(EndpointPathNode pathNode, List<EndpointStructureNode> children) {
        this.pathNode = pathNode;
        this.children = children;
    }

    public List<EndpointStructureNode> getChildren() {
        return children;
    }

    public void addChild(EndpointStructureNode structureNode) {
        this.children.add(structureNode);
    }

    public EndpointPathNode getPathNode() {
        return pathNode;
    }

    public boolean matchesUrlPath(String urlPath) {

        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        if (urlPath.endsWith("/")) {
            urlPath = urlPath.substring(0, urlPath.length() - 1);
        }

        int firstPartStartIndex = urlPath.indexOf('/');

        String firstPart = firstPartStartIndex < 0
                ? urlPath
                : urlPath.substring(0, firstPartStartIndex);

        String remainder = firstPartStartIndex < 0
                ? ""
                : urlPath.substring(firstPartStartIndex);

        if (this.pathNode == null || !this.pathNode.matches(firstPart)) {
            return false;
        }

        if (remainder.length() == 0) {
            return true;
        }

        boolean childMatches = false;
        for (int i = 0; i < children.size() && !childMatches; i++) {
            childMatches = children.get(i).matchesUrlPath(remainder);
        }
        return childMatches;
    }

    @Override
    public String toString() {
        return pathNode.toString() + " (" + children.size() + " children)";
    }
}
