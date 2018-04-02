package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import java.util.*;

public class EndpointStructure {
    List<EndpointStructureNode> rootNodes = new ArrayList<>();

    public void acceptEndpointPath(List<EndpointPathNode> pathNodes) {
        EndpointStructureNode parent = null;
        Queue<EndpointPathNode> pendingNodes = new LinkedList<>(pathNodes);

        while (pendingNodes.size() > 0) {
            EndpointPathNode node = pendingNodes.remove();

            if (parent == null) {
                for (EndpointStructureNode rootNode : rootNodes) {
                    if (rootNode.getPathNode().matches(node)) {
                        parent = rootNode;
                    }
                }
                if (parent == null) {
                    parent = new EndpointStructureNode(node);
                    rootNodes.add(parent);
                }
            } else {
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
                }
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
}
