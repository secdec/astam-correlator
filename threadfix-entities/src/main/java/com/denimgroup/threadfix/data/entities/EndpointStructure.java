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

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import java.util.*;

public class EndpointStructure extends EndpointStructureNode {
    public EndpointStructure() {
        super(null);
    }

    public void acceptEndpointPath(List<EndpointPathNode> pathNodes) {
        EndpointStructureNode parent = this;
        Queue<EndpointPathNode> pendingNodes = new LinkedList<>(pathNodes);

        while (pendingNodes.size() > 0) {
            EndpointPathNode node = pendingNodes.remove();
            EndpointStructureNode newParent = null;
            for (EndpointStructureNode subNode : parent.getChildren()) {
                if (subNode.getPathNode().matches(node)) {
                    newParent = subNode;
                }
            }
            if (newParent == null) {
                EndpointStructureNode newNode = new EndpointStructureNode(node);
                parent.addChild(newNode);
                parent = newNode;
            } else {
                parent = newParent;
            }
        }
    }

    public void acceptEndpoint(Endpoint endpoint) {
        acceptEndpointPath(endpoint.getUrlPathNodes());
    }

    public void acceptAllEndpoints(Collection<Endpoint> endpoints) {
        for (Endpoint endpoint : endpoints) {
            acceptEndpointPath(endpoint.getUrlPathNodes());
        }
    }

    @Override
    public boolean matchesUrlPath(String urlPath) {
        for (EndpointStructureNode subNode : getChildren()) {
            if (subNode.matchesUrlPath(urlPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "EndpointStructure (" + getChildren().size() + " root nodes)";
    }
}
