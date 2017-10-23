package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;

import java.util.Collection;

public abstract class Mapper {

    StrutsConfigurationProperties config;
    Collection<StrutsPackage> packages;

    public Mapper(StrutsConfigurationProperties configProperties, Collection<StrutsPackage> packages) {
        this.config = configProperties;
        this.packages = packages;
    }

    public abstract String mapEndpointToJsp(String endpoint);
    public abstract StrutsAction mapEndpointToAction(String endpoint);

    public abstract String mapJspToEndpoint(String jspLocation);
    public abstract String mapActionToEndpoint(StrutsAction action);


    protected StrutsPackage findPackageForAction(StrutsAction action) {
        for (StrutsPackage pkg : packages) {
            if (pkg.getActions().contains(action)) {
                return pkg;
            }
        }

        return null;
    }

}
