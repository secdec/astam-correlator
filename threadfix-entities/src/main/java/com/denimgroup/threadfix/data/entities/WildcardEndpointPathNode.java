package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import javax.annotation.Nonnull;

public class WildcardEndpointPathNode implements EndpointPathNode {

    private String wildcardPattern;

    public WildcardEndpointPathNode(String pattern) {
        this.wildcardPattern = pattern;
    }

    @Override
    public boolean matches(@Nonnull String pathPart) {
        return true;
    }

    @Override
    public boolean matches(@Nonnull EndpointPathNode node) {
        return true;
    }

    public boolean hasPattern() {
        return wildcardPattern != null;
    }

    public String getPattern() {
        return wildcardPattern;
    }

    @Override
    public String toString() {
        return "wildcard";
    }
}
