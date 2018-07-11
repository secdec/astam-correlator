package com.denimgroup.threadfix.framework.engine.full;

import org.codehaus.jackson.map.ObjectMapper;
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
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    public static String serialize(Endpoint endpoint) throws IOException {
        return serialize(getEndpointFrameworkType(endpoint), endpoint);
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

    public static String serializeAll(Collection<Endpoint> endpoints) throws IOException {
        //  Manually construct a JSON array of the endpoints
        List<EndpointSerializationWrapper> wrappers = new ArrayList<EndpointSerializationWrapper>(endpoints.size());
        for (Endpoint endpoint : endpoints) {
            EndpointSerializationWrapper wrapper = new EndpointSerializationWrapper();
            wrapper.endpointFrameworkType = getEndpointFrameworkType(endpoint);

            EndpointSerializer serializer = getSerializer(wrapper.endpointFrameworkType);
            if (serializer == null) {
                continue;
            }

            wrapper.serializedEndpoint = serializer.serialize(endpoint);
            wrappers.add(wrapper);
        }

        return DefaultMapper.writeValueAsString(wrappers);
    }

    public static Endpoint deserialize(String serializedEndpoint) throws IOException {
        EndpointSerializationWrapper endpointWrapper = DefaultMapper.readValue(serializedEndpoint, EndpointSerializationWrapper.class);

        EndpointSerializer serializer = getSerializer(endpointWrapper.endpointFrameworkType);
        if (serializer == null) {
            return null;
        }

        return serializer.deserialize(endpointWrapper.serializedEndpoint);
    }

    public static Endpoint[] deserializeAll(String serializedEndpoints) throws IOException {
        EndpointSerializationWrapper[] endpointWrappers = DefaultMapper.readValue(serializedEndpoints, TypeFactory.defaultInstance().constructArrayType(EndpointSerializationWrapper.class));

        Endpoint[] result = new Endpoint[endpointWrappers.length];
        for (int i = 0; i < endpointWrappers.length; i++) {
            EndpointSerializer serializer = getSerializer(endpointWrappers[i].endpointFrameworkType);
            if (serializer == null) {
                continue;
            }
            Endpoint endpoint = serializer.deserialize(endpointWrappers[i].serializedEndpoint);
            result[i] = endpoint;
        }

        return result;
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
