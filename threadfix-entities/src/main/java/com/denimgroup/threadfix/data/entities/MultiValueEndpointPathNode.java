package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.interfaces.EndpointPathNode;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MultiValueEndpointPathNode implements EndpointPathNode {

    private Collection<String> possibleValues;

    public MultiValueEndpointPathNode(@Nonnull Collection<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public boolean matches(@Nonnull String pathPart) {
        for (String option : possibleValues) {
            if (option.equalsIgnoreCase(pathPart)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matches(@Nonnull EndpointPathNode node) {
        if (!(node instanceof MultiValueEndpointPathNode)) {
            return false;
        } else {
            Collection<String> otherValues = ((MultiValueEndpointPathNode) node).possibleValues;
            return otherValues.containsAll(this.possibleValues) && this.possibleValues.containsAll(otherValues);
        }
    }

    @Override
    public String toString() {
        return "(multi-value)";
    }
}
