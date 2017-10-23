package com.denimgroup.threadfix.framework.impl.struts.conventions;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;

import java.util.Collection;

public abstract class Convention {

    StrutsConfigurationProperties config;
    Collection<StrutsPackage> packages;

    String actionSuffix;

    public Convention(StrutsConfigurationProperties config, Collection<StrutsPackage> packages) {
        this.config = config;
        this.packages = packages;

        this.actionSuffix = config.get("struts.convention.action.suffix"); // ie "Controller"
    }

}
