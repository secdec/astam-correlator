package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.plugins.StrutsKnownPlugins;
import com.denimgroup.threadfix.framework.impl.struts.StrutsProject;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;

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
        Collection<StrutsKnownPlugins> availablePlugins = project.getPlugins();

        if (isMapperConfigured()) {
            String mapperName = config.get("struts.mapper.class");

            result = findMapper(mapperName, project);
        }

        if (result == null){

            if (availablePlugins.contains(StrutsKnownPlugins.CONVENTION)) {

                CompositeActionMapper compositeMapper = new CompositeActionMapper();

                //  If no mappers manually configured an Convention plugin is enabled, struts Default mapper should
                //  be set as primary mapper and Convention mapper as backup
                compositeMapper.addMapper(new DefaultActionMapper());
                compositeMapper.addMapper(new ConventionPluginMapper());



                if (availablePlugins.contains(StrutsKnownPlugins.STRUTS2_REST)) {
                    compositeMapper.addMapper(new RestPluginActionMapper());
                }

                result = compositeMapper;

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

        //  TODO - There's gotta' be a better way to do this

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
        else if (mapperName.equalsIgnoreCase("restful2")
                || mapperName.equalsIgnoreCase("org.apache.struts2.dispatcher.mapper.Restful2ActionMapper")) {

            result = new Restful2ActionMapper();

        }

        return result;
    }
}
