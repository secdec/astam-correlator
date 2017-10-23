package com.denimgroup.threadfix.framework.impl.struts.conventions;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import java.util.Map;

public class ConventionFactory {

    private static final SanitizedLogger log = new SanitizedLogger("ConventionFactory");

    StrutsConfigurationProperties config;

    public ConventionFactory(StrutsConfigurationProperties config) {
        this.config = config;
    }

    public boolean isConventionConfigured() {
        Map<String, String> properties = config.getAllProperties();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().contains("struts.convention")) {
                return true;
            }
        }

        return false;
    }

}
