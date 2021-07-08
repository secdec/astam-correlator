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

package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.EndpointSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

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
