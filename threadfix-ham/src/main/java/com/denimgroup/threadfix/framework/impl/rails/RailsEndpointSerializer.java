package com.denimgroup.threadfix.framework.impl.rails;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;

import java.io.IOException;

public class RailsEndpointSerializer extends EndpointSerializer {
    @Override
    public String serialize(Endpoint endpoint) throws IOException {
        return defaultSerialize(endpoint);
    }

    @Override
    public Endpoint deserialize(String serializedEndpoint) throws IOException {
        return defaultDeserialize(serializedEndpoint, RailsEndpoint.class);
    }
}
