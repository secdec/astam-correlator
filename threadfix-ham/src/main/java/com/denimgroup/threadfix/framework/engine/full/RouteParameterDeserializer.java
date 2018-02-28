package com.denimgroup.threadfix.framework.engine.full;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.entities.RouteParameterType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
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
