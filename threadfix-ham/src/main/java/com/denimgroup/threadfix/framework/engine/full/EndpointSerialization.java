package com.denimgroup.threadfix.framework.engine.full;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;
import com.denimgroup.threadfix.framework.impl.django.DjangoEndpoint;
import com.denimgroup.threadfix.framework.impl.django.DjangoEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetEndpoint;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointExplicit;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointExtensionless;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointImplicit;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.jsp.JSPEndpoint;
import com.denimgroup.threadfix.framework.impl.jsp.JSPEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.rails.RailsEndpoint;
import com.denimgroup.threadfix.framework.impl.rails.RailsEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.spring.SpringControllerEndpoint;
import com.denimgroup.threadfix.framework.impl.spring.SpringControllerEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpoint;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpointSerializer;

import java.io.IOException;

public class EndpointSerialization {

    private static EndpointSerializer getSerializer(FrameworkType frameworkType) {
        switch (frameworkType) {
            case PYTHON:            return new DjangoEndpointSerializer();
            case RAILS:             return new RailsEndpointSerializer();
            case STRUTS:            return new StrutsEndpointSerializer();
            case DOT_NET_WEB_FORMS: return new WebFormsEndpointSerializer();
            case DOT_NET_MVC:       return new DotNetEndpointSerializer();
            case JSP:               return new JSPEndpointSerializer();
            case SPRING_MVC:        return new SpringControllerEndpointSerializer();

            default:
                return null;
        }
    }

    public static String serialize(Endpoint endpoint) throws IOException {
        return serialize(getEndpointFrameworkType(endpoint), endpoint);
    }

    public static String serialize(FrameworkType frameworkType, Endpoint endpoint) throws IOException {
        EndpointSerializer serializer = getSerializer(frameworkType);
        if (serializer == null) {
            return null;
        }
        return serializer.serialize(endpoint);
    }

    public static Endpoint deserialize(FrameworkType frameworkType, String serializedEndpoint) throws IOException {
        EndpointSerializer serializer = getSerializer(frameworkType);
        if (serializer == null) {
            return null;
        }
        return serializer.deserialize(serializedEndpoint);
    }

    public static FrameworkType getEndpointFrameworkType(Endpoint endpoint) {
        if (endpoint instanceof DjangoEndpoint) {
            return FrameworkType.PYTHON;
        } else if (endpoint instanceof DotNetEndpoint) {
            return FrameworkType.DOT_NET_MVC;
        } else if (endpoint instanceof WebFormsEndpointImplicit ||
                   endpoint instanceof WebFormsEndpointExplicit ||
                   endpoint instanceof WebFormsEndpointExtensionless) {
            return FrameworkType.DOT_NET_WEB_FORMS;
        } else if (endpoint instanceof JSPEndpoint) {
            return FrameworkType.JSP;
        } else if (endpoint instanceof RailsEndpoint) {
            return FrameworkType.RAILS;
        } else if (endpoint instanceof SpringControllerEndpoint) {
            return FrameworkType.SPRING_MVC;
        } else if (endpoint instanceof StrutsEndpoint) {
            return FrameworkType.STRUTS;
        } else {
            return FrameworkType.NONE;
        }
    }
}
