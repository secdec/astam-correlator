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

package com.denimgroup.threadfix.framework.engine.full;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RouteParameterDeserializer extends StdDeserializer<RouteParameter> {

    public RouteParameterDeserializer() {
        this(null);
    }

    public RouteParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public RouteParameter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        List<String> acceptedValues = null;

        String typeSource = null;
        if (!(node.get("dataTypeSource") instanceof NullNode)) {
            typeSource = node.get("dataTypeSource").asText();
        }
        String name = node.get("name").asText();

        RouteParameterType paramType = RouteParameterType.valueOf(node.get("paramType").asText());
        JsonNode acceptedValuesNode = node.get("acceptedValues");
        if (acceptedValuesNode != null && acceptedValuesNode instanceof ArrayNode) {
            acceptedValues = list();
            for (JsonNode listNode : acceptedValuesNode) {
                acceptedValues.add(listNode.asText());
            }
        }

        RouteParameter newParam = new RouteParameter(name);
        newParam.setAcceptedValues(acceptedValues);
        newParam.setDataType(typeSource);
        newParam.setParamType(paramType);
        return newParam;
    }
}
