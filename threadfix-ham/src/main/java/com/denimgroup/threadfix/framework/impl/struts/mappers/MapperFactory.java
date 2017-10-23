package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Collection;

public class MapperFactory {

    private static final SanitizedLogger log = new SanitizedLogger("MapperFactory");

    StrutsConfigurationProperties config;

    public MapperFactory(StrutsConfigurationProperties config) {
        this.config = config;
    }

    public boolean isMapperConfigured() {
        return config.get("struts.mapper.class") != null;
    }

    public Mapper detectMapper(Collection<StrutsPackage> packages) {
        Mapper result = null;

        if (isMapperConfigured()) {
            String mapper = config.get("struts.mapper.class");

            String[] mapperParts = mapper.split(".");
            String mapperClassName = mapperParts[mapperParts.length - 1].toLowerCase();

            if (mapperClassName.contains("composite")) {
                result = new CompositeActionMapper(config, packages);
            } else if (mapperClassName.contains("rest")) {
                result = new RestActionMapper(config, packages);
            } else if (mapperClassName.contains("prefixbased")) {
                result = new PrefixBasedActionMapper(config, packages);
            } else {
                log.warn("Unknown mapper name " + mapperClassName + ", falling back to DefaultActionMapper");
                result = new DefaultActionMapper(config, packages);
            }


        } else {
            result = new DefaultActionMapper(config, packages);
        }

        return result;
    }

}
