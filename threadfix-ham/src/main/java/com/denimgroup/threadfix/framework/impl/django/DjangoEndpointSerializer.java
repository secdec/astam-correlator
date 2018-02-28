package com.denimgroup.threadfix.framework.impl.django;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;

import java.io.IOException;

public class DjangoEndpointSerializer extends EndpointSerializer {
    @Override
    public String serialize(Endpoint endpoint) throws IOException {
        return defaultSerialize(endpoint);
    }

    @Override
    public Endpoint deserialize(String serializedEndpoint) throws IOException {
        return defaultDeserialize(serializedEndpoint, DjangoEndpoint.class);
    }
}
