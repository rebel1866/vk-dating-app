package com.melnikov.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonParser<T> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public T parseJson(String source, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(source, typeReference);
    }

    public static String getStringValue(String json, String key) throws IOException {
        ObjectNode node = objectMapper.readValue(json, ObjectNode.class);
        JsonNode result = node.findValue(key);
        if (result != null) {
            return result.textValue();
        }
        throw new IOException("Value by key not found. Key: " + key);
    }
    public static int getIntegerValue(String json, String key) throws IOException {
        ObjectNode node = objectMapper.readValue(json, ObjectNode.class);
        JsonNode result = node.findValue(key);
        if (result != null) {
            return result.intValue();
        }
        throw new IOException("Value by key not found. Key: " + key);
    }
}
