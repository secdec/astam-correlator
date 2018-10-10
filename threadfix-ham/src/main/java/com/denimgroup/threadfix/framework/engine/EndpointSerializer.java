////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

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
