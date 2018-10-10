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

package com.denimgroup.threadfix.framework.engine;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class BaseEndpointDetector {

    private List<String> samples = list();

    public void addSample(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }
        samples.add(endpoint);
    }

    public String detectBaseEndpoint() {
        List<String> mostCommonParts = list();
        for (String sample : samples) {
            String[] parts = StringUtils.split(sample, '/');
            if (mostCommonParts.isEmpty()) {
                mostCommonParts.addAll(Arrays.asList(parts));
            } else{
                while (parts.length < mostCommonParts.size()) {
                    mostCommonParts.remove(mostCommonParts.size() - 1);
                }

                int differentIndex = -1;
                for (int i = 0; i < parts.length && i < mostCommonParts.size(); i++) {
                    String samplePart = parts[i];
                    String commonPart = mostCommonParts.get(i);
                    if (!samplePart.equalsIgnoreCase(commonPart)) {
                        differentIndex = i;
                        break;
                    }
                }

                if (differentIndex >= 0) {
                    while (mostCommonParts.size() > differentIndex) {
                        mostCommonParts.remove(mostCommonParts.size() - 1);
                    }
                }
            }
        }
        return "/" + StringUtils.join(mostCommonParts, "/");
    }

}
