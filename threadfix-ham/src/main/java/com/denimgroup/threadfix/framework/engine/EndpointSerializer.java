package com.denimgroup.threadfix.framework.engine;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.RouteParameterDeserializer;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import java.io.IOException;

/**
 * Provides serialization to and from a framework-specific endpoint type, ensuring
 * that the deserialized Endpoint will be of the original type. This is required
 * for persisting endpoints that are later used with the 'compareRelevance' method.
 */
public abstract class EndpointSerializer {

    ObjectMapper mapper = null;

    public EndpointSerializer() {
        mapper = new ObjectMapper();

        mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);

        SimpleModule module = new SimpleModule("RouteParameterDeserializer", Version.unknownVersion());
        module.addDeserializer(RouteParameter.class, new RouteParameterDeserializer());
        mapper.registerModule(module);
    }

    public void configure(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    protected ObjectMapper getMapper() {
        return this.mapper;
    }

    public abstract String serialize(Endpoint endpoint) throws IOException;
    public abstract Endpoint deserialize(String serializedEndpoint) throws IOException;

    protected String defaultSerialize(Endpoint endpoint) throws IOException {
        return mapper.writeValueAsString(endpoint);
    }

    protected <T extends Endpoint> Endpoint defaultDeserialize(String serializedEndpoint, Class<T> type) throws IOException {
        return mapper.readValue(serializedEndpoint, type);
    }
}
