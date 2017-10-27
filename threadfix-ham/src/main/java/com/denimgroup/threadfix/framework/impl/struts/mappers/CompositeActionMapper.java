package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class CompositeActionMapper implements ActionMapper {

    static SanitizedLogger log = new SanitizedLogger(CompositeActionMapper.class.getName());

    Collection<ActionMapper> subMappers;

    public CompositeActionMapper() {
        this.subMappers = list();
    }

    public CompositeActionMapper(Collection<ActionMapper> subMappers) {
        this.subMappers = list();
        this.subMappers.addAll(subMappers);
    }

    public CompositeActionMapper(StrutsProject project) {
        subMappers = list();

        StrutsConfigurationProperties config = project.getConfig();
        String allMappers = config.get("struts.mapper.composite");

        ActionMapperFactory mapperFactory = new ActionMapperFactory(config);

        String[] mapperNames = allMappers.split(",");
        for (String name : mapperNames) {
            ActionMapper mapper = mapperFactory.findMapper(name, project);
            if (mapper == null) {
                log.warn("Couldn't find action mapper with name " + name);
            } else {
                subMappers.add(mapper);
            }
        }
    }

    public void addMapper(ActionMapper mapper) {
        this.subMappers.add(mapper);
    }

    @Override
    public List<StrutsEndpoint> generateEndpoints(StrutsProject project, String namespace) {

        List<StrutsEndpoint> endpoints = list();

        for (ActionMapper mapper : subMappers) {
            Collection<StrutsEndpoint> subEndpoints = mapper.generateEndpoints(project, namespace);
            if (subEndpoints != null)
                endpoints.addAll(subEndpoints);
        }

        return endpoints;
    }

    @Override
    public Collection<StrutsKnownPlugins> getRequiredPlugins() {
        List<StrutsKnownPlugins> plugins = list();
        for (ActionMapper mapper : subMappers) {
            plugins.addAll(mapper.getRequiredPlugins());
        }
        return plugins;
    }
}
