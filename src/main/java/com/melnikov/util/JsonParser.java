package com.melnikov.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonParser<T> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public T parseJson(String source, Class <T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(source, clazz);
    }
}
