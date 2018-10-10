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

package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetEndpoint;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointExplicit;
import com.denimgroup.threadfix.framework.impl.jsp.JSPEndpoint;
import com.denimgroup.threadfix.framework.impl.spring.SpringControllerEndpoint;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class EndpointUtil {

    public static List<Endpoint> flattenWithVariants(@Nonnull Collection<Endpoint> endpoints) {
        List<Endpoint> result = list();
        Queue<Endpoint> pendingEndpoints = new ArrayDeque<Endpoint>(endpoints);

        while (!pendingEndpoints.isEmpty()) {
            Endpoint current = pendingEndpoints.remove();
            if (!result.contains(current)) {
                result.add(current);
                pendingEndpoints.addAll(current.getVariants());
            }
        }
        return result;
    }



    public interface VariantRectifier {
        void setPrimaryVariant(Endpoint assignee, Endpoint primaryVariant);
    }

    public static void rectifyVariantHierarchy(Collection<Endpoint> endpoints) {
        rectifyVariantHierarchy(endpoints, new VariantRectifier() {
            @Override
            public void setPrimaryVariant(Endpoint assignee, Endpoint primaryVariant) {
                ((AbstractEndpoint)assignee).setPrimaryVariant(primaryVariant);
            }
        });
    }

    public static void rectifyVariantHierarchy(Collection<Endpoint> endpoints, VariantRectifier rectifier) {
        List<Endpoint> visitedEndpoints = list();
        Queue<Endpoint> remainingEndpoints = new ArrayDeque<Endpoint>(endpoints);
        while (!remainingEndpoints.isEmpty()) {
            Endpoint next = remainingEndpoints.remove();
            if (visitedEndpoints.contains(next)) {
                continue;
            } else {
                visitedEndpoints.add(next);
            }
            for (Endpoint variant : next.getVariants()) {
                rectifier.setPrimaryVariant(variant, next);
                remainingEndpoints.add(variant);
            }
        }
    }


    private static class EndpointSpec {
        int startLine, endLine;
        String filePath, httpMethod;

        public static EndpointSpec fromEndpoint(Endpoint endpoint)
        {
            EndpointSpec spec = new EndpointSpec();
            spec.startLine = endpoint.getStartingLineNumber();
            spec.endLine = endpoint.getEndingLineNumber();
            spec.filePath = endpoint.getFilePath();
            spec.httpMethod = endpoint.getHttpMethod();
            return spec;
        }

        @Override
        public int hashCode() {
            return startLine ^ (endLine << 16) ^ filePath.hashCode() ^ httpMethod.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(EndpointSpec.class) && obj.hashCode() == this.hashCode();
        }
    }

    //  Assigns variants to their primary endpoint and returns the set of primary endpoints
    public static List<Endpoint> autoAssignEndpointVariants(Collection<Endpoint> endpoints) {
        Map<EndpointSpec, List<Endpoint>> variantEndpointGroups = new HashMap<EndpointSpec, List<Endpoint>>();
        for (Endpoint endpoint : endpoints) {
            EndpointSpec spec = EndpointSpec.fromEndpoint(endpoint);
            List<Endpoint> group = variantEndpointGroups.get(spec);
            if (group == null) {
                group = list();
                variantEndpointGroups.put(spec, group);
            }
            group.add(endpoint);
        }

        List<Endpoint> primaryEndpoints = list();
        for (List<Endpoint> group : variantEndpointGroups.values()) {
            Endpoint bestEndpoint = null;
            for (Endpoint member : group) {
                if (bestEndpoint == null || member.getUrlPath().length() < bestEndpoint.getUrlPath().length()) {
                    bestEndpoint = member;
                }
            }

            AbstractEndpoint abstractEndpoint = (AbstractEndpoint)bestEndpoint;
            for (Endpoint member : group) {
                if (member != bestEndpoint) {
                    abstractEndpoint.addVariant(member);
                    ((AbstractEndpoint)member).setPrimaryVariant(abstractEndpoint);
                }
            }

            primaryEndpoints.add(bestEndpoint);
        }

        return primaryEndpoints;
    }
}
