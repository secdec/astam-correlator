package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.PathUtil;
import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.swing.*;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class PrefixBasedActionMapper implements ActionMapper {

    static SanitizedLogger log = new SanitizedLogger(ActionMapper.class.getName());

    Map<String, ActionMapper> delegateActionMappers;
    StrutsProject project;
    StrutsConfigurationProperties config;

    public PrefixBasedActionMapper(StrutsProject project) {
        this.project = project;
        this.config = project.getConfig();

        delegateActionMappers = new HashMap<String, ActionMapper>();

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


    ActionMapper findBestMapper(String forEndpoint) {
        int bestMapperLength = -1;
        ActionMapper relevantMapper = null;
        for (Map.Entry<String, ActionMapper> mapping : delegateActionMappers.entrySet()) {
            if (forEndpoint.startsWith(mapping.getKey())) {
                int endpointLength = mapping.getKey().length();
                if (endpointLength > bestMapperLength) {
                    bestMapperLength = endpointLength;
                    relevantMapper = mapping.getValue();
                }
            }
        }
        return relevantMapper;
    }

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String namespace) {

        List<StrutsEndpoint> endpoints = list();

        for (Map.Entry<String, ActionMapper> namespacedMapper : delegateActionMappers.entrySet()) {
            String mapperNamespace = namespacedMapper.getKey();
            ActionMapper mapper = namespacedMapper.getValue();

            String combinedNamespace = PathUtil.combine(namespace, mapperNamespace);

            Collection<StrutsEndpoint> mapperEndpoints = mapper.generateEndpoints(project, combinedNamespace);
            if (mapperEndpoints != null)
                endpoints.addAll(mapperEndpoints);
        }

        return endpoints;
    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        List<StrutsKnownPlugins> plugins = list();
        for (ActionMapper mapping : delegateActionMappers.values()) {
            plugins.addAll(mapping.getRequiredPlugins());
        }
        return plugins;
    }
}
