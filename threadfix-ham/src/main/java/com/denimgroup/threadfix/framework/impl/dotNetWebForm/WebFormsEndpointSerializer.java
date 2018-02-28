package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class WebFormsEndpointSerializer extends EndpointSerializer {
    @Override
    public String serialize(Endpoint endpoint) throws IOException {
        ObjectMapper mapper = getMapper();

        WebFormsEndpointSerializationWrapper wrapper = new WebFormsEndpointSerializationWrapper();
        if (endpoint instanceof WebFormsEndpointExplicit) {
            wrapper.endpointType = WebFormsEndpointType.EXPLICIT;
        } else if (endpoint instanceof WebFormsEndpointExtensionless) {
            wrapper.endpointType = WebFormsEndpointType.EXTENSIONLESS;
        } else if (endpoint instanceof WebFormsEndpointImplicit) {
            wrapper.endpointType = WebFormsEndpointType.IMPLICIT;
        } else {
            return null;
        }

        wrapper.serializedEndpoint = mapper.writeValueAsString(endpoint);

        return mapper.writeValueAsString(wrapper);
    }

    @Override
    public Endpoint deserialize(String serializedEndpoint) throws IOException {
        ObjectMapper mapper = getMapper();

        WebFormsEndpointSerializationWrapper wrapper = mapper.readValue(serializedEndpoint, WebFormsEndpointSerializationWrapper.class);

        String endpointJson = wrapper.serializedEndpoint;
        switch (wrapper.endpointType) {
            case EXPLICIT: return mapper.readValue(endpointJson, WebFormsEndpointExplicit.class);
            case IMPLICIT: return mapper.readValue(endpointJson, WebFormsEndpointImplicit.class);
            case EXTENSIONLESS: return mapper.readValue(endpointJson, WebFormsEndpointExtensionless.class);

            default:
                return null;
        }
    }
}
