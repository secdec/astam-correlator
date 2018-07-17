package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class WildcardEndpointPathNode implements EndpointPathNode {

    private Pattern wildcardPattern;

    public WildcardEndpointPathNode(String pattern) {
    	if (pattern == null) {
    		this.wildcardPattern = Pattern.compile(".*");
	    } else {
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
