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
