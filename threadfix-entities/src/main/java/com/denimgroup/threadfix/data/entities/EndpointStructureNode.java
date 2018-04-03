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
