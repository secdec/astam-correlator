////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PrefixBasedActionMapper implements ActionMapper {

    static SanitizedLogger log = new SanitizedLogger(ActionMapper.class.getName());

    SortedMap<String, ActionMapper> delegateActionMappers;
    StrutsProject project;
    StrutsConfigurationProperties config;

    public PrefixBasedActionMapper(StrutsProject project) {
        this.project = project;
        this.config = project.getConfig();

        delegateActionMappers = new TreeMap<String, ActionMapper>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        String allMappings = config.get("struts.mapper.prefixMapping");

        ActionMapperFactory mapperFactory = new ActionMapperFactory(config);

        String[] separatedMappings = allMappings.split(",");
        for (String mapping : separatedMappings) {
            String[] mappingParts = mapping.split(":");
            String endpoint = mappingParts[0];
            String mapperName = mappingParts[1];
            ActionMapper mapper = mapperFactory.findMapper(mapperName, project);
            if (mapper == null) {
                log.warn("Couldn't find mapper with name " + mapperName);
            } else {
                delegateActionMappers.put(endpoint, mapper);
            }
        }
    }

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, Collection<StrutsPackage> packages, String namespace) {

        List<StrutsEndpoint> endpoints = list();

        for (Map.Entry<String, ActionMapper> namespacedMapper : delegateActionMappers.entrySet()) {
            String mapperNamespace = namespacedMapper.getKey();
            ActionMapper mapper = namespacedMapper.getValue();

            String combinedNamespace = PathUtil.combine(namespace, mapperNamespace);

            List<StrutsPackage> packagesForMapper = list();
            for (StrutsPackage strutsPackage : packages) {
                if (strutsPackage.getNamespace().startsWith(combinedNamespace)) {
                    packagesForMapper.add(strutsPackage);
                }
            }

            Collection<StrutsEndpoint> originalEndpoints = new ArrayList<StrutsEndpoint>(endpoints);
            Collection<StrutsEndpoint> mapperEndpoints = mapper.generateEndpoints(project, packagesForMapper, namespace);
            if (mapperEndpoints != null) {
                //  Ignore endpoints handled by previous (more specific) mappers
                for (StrutsEndpoint detectedEndpoint : mapperEndpoints) {
                    boolean isNew = true;
                    for (StrutsEndpoint existingEndpoint : originalEndpoints) {
                        if (existingEndpoint.getUrlPath().equals(detectedEndpoint.getUrlPath()) ||
                                existingEndpoint.getFilePath().equalsIgnoreCase(detectedEndpoint.getFilePath())) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        endpoints.add(detectedEndpoint);
                    }
                }
            }
        }

        return endpoints;
    }
}
