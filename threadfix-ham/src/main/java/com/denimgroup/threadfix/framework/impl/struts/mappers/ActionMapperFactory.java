package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsRestPlugin;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import static com.denimgroup.threadfix.CollectionUtils.list;

/*
    CompositeActionMapper:
        Common name: composite
        Full name: org.apache.struts2.dispatcher.mapper.CompositeActionMapper

    DefaultActionMapper:
        Common name: struts
        Full name: org.apache.struts2.dispatcher.mapper.DefaultActionMapper

    PrefixBasedActionMapper:
        Common name: prefix (?) prefixBased (?) (can't find a reference)
        Full name: org.apache.struts2.dispatcher.mapper.PrefixBasedActionMapper

    RestPluginActionMapper:
        Common name: rest
        Full name: org.apache.struts2.rest.RestActionMapper

    Restful2ActionMapper:
        Common name: restful2
        Full name: org.apache.struts2.dispatcher.mapper.Restful2ActionMapper

 */

public class ActionMapperFactory {

    private static final SanitizedLogger log = new SanitizedLogger("ActionMapperFactory");

    StrutsConfigurationProperties config;

    public ActionMapperFactory(StrutsConfigurationProperties config) {
        this.config = config;
    }

    public boolean isMapperConfigured() {
        return config.get("struts.mapper.class") != null;
    }

    public ActionMapper detectMapper(StrutsProject project) {
        ActionMapper result = null;

        if (isMapperConfigured()) {
            String mapperName = config.get("struts.mapper.class");

            result = findMapper(mapperName, project);
        } else {

            if (project.hasPlugin(StrutsRestPlugin.class)) {
                result = new CompositeActionMapper(list(new DefaultActionMapper(), new RestPluginActionMapper()));
            } else {
                log.debug("No mapper configuration detected, using DefaultActionMapper");
                result = new DefaultActionMapper();
            }
        }

        return result;
    }

    ActionMapper findMapper(String mapperName, StrutsProject forProject) {
        ActionMapper result = null;

        if (mapperName == null) {
            return null;
        }

        if (mapperName.equalsIgnoreCase("composite")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.CompositeActionMapper")) {

            result = new CompositeActionMapper(forProject);

        }
        else if (mapperName.equalsIgnoreCase("struts")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.DefaultActionMapper")) {

            result = new DefaultActionMapper();

        }
        else if (mapperName.equalsIgnoreCase("prefix") || mapperName.equalsIgnoreCase("prefixBased")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.PrefixBasedActionMapper")) {

            result = new PrefixBasedActionMapper(forProject);

        }
        else if (mapperName.equalsIgnoreCase("rest")
                || mapperName.equalsIgnoreCase("org.apache.struts2.rest.RestActionMapper")) {

            result = new RestPluginActionMapper();

        }

        return result;
    }
}
