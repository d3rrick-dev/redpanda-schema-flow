package com.d3rrick.kafkawithavro.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static ObjectMapper newObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        return configureObjectMapper(objectMapper);
    }

    public static ObjectMapper configureObjectMapper(final ObjectMapper objectMapper) {
        return objectMapper;
    }

}
