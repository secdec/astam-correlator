package com.denimgroup.threadfix.framework.impl.struts.mappers;

import com.denimgroup.threadfix.framework.impl.struts.StrutsConfigurationProperties;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;

import java.util.Collection;
import java.util.regex.Pattern;

public class DefaultActionMapper extends Mapper {

    String allowedActionNames;
    String allowedMethodNames;
    String defaultActionName;
    String defaultMethodName;

    public DefaultActionMapper(StrutsConfigurationProperties configProperties, Collection<StrutsPackage> packages) {
        super(configProperties, packages);

        defaultActionName = config.get("struts.default.action.name");
        defaultMethodName = config.get("struts.default.method.name");
        allowedActionNames = config.get("struts.allowed.action.names");
        allowedMethodNames = config.get("struts.allowed.method.names");
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







    boolean isAllowedActionName(String endpoint) {
        if (allowedActionNames == null) {
            return true;
        } else {
            return Pattern.compile(allowedActionNames).matcher(endpoint).matches();
        }
    }

    boolean isAllowedMethodName(String endpoint) {
        if (allowedMethodNames == null) {
            return true;
        } else {
            return Pattern.compile(allowedMethodNames).matcher(endpoint).matches();
        }
    }
}
