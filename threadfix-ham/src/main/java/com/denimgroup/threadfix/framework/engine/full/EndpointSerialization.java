package com.denimgroup.threadfix.framework.engine.full;

import org.codehaus.jackson.map.ObjectMapper;
import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;
import com.denimgroup.threadfix.framework.impl.django.DjangoEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.dotNet.DotNetEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.dotNetWebForm.WebFormsEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.jsp.JSPEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.rails.RailsEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.spring.SpringControllerEndpointSerializer;
import com.denimgroup.threadfix.framework.impl.struts.StrutsEndpointSerializer;

import java.io.IOException;

public class EndpointSerialization {

    private static ObjectMapper DefaultMapper = new ObjectMapper();

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

    public static String serialize(FrameworkType frameworkType, Endpoint endpoint) throws IOException {
        EndpointSerializer serializer = getSerializer(frameworkType);
        if (serializer == null) {
            return null;
        }
        String serializedEndpoint = serializer.serialize(endpoint);
        EndpointSerializationWrapper endpointWrapper = new EndpointSerializationWrapper();
        endpointWrapper.endpointFrameworkType = frameworkType;
        endpointWrapper.serializedEndpoint = serializedEndpoint;
        return DefaultMapper.writeValueAsString(endpointWrapper);
    }

    public static Endpoint deserialize(String serializedEndpoint) throws IOException {
        EndpointSerializationWrapper endpointWrapper = DefaultMapper.readValue(serializedEndpoint, EndpointSerializationWrapper.class);

        EndpointSerializer serializer = getSerializer(endpointWrapper.endpointFrameworkType);
        if (serializer == null) {
            return null;
        }

        return serializer.deserialize(endpointWrapper.serializedEndpoint);
    }
}
