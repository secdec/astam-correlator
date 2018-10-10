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
