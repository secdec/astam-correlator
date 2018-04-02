package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import javax.annotation.Nonnull;

public final class ExplicitEndpointPathNode implements EndpointPathNode {

    private String pathPart;

    public ExplicitEndpointPathNode(String pathPart) {
        this.pathPart = pathPart;
    }

    @Override
    public boolean matches(@Nonnull String pathPart) {
        return pathPart.equalsIgnoreCase(this.pathPart);
    }

    @Override
    public boolean matches(@Nonnull EndpointPathNode node) {
        if (node instanceof ExplicitEndpointPathNode) {
            return matches(((ExplicitEndpointPathNode) node).pathPart);
        } else {
            return node.matches(this);
        }
    }

    @Override
    public String toString() {
        return "explicit: " + this.pathPart;
    }
}
