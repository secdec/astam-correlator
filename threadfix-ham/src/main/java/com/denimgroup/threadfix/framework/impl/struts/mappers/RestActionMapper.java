package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;

import java.util.Collection;

public class RestActionMapper extends Mapper {
    public RestActionMapper(StrutsConfigurationProperties configProperties, Collection<StrutsPackage> packages) {
        super(configProperties, packages);
    }

    @Override
    public String mapEndpointToJsp(String endpoint) {
        return null;
    }

    @Override
    public StrutsAction mapEndpointToAction(String endpoint) {
        return null;
    }

    @Override
    public String mapJspToEndpoint(String jspLocation) {
        return null;
    }

    @Override
    public String mapActionToEndpoint(StrutsAction action) {
        return null;
    }
}
