package com.integrationhub.dashboard.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the mixed-type "duplicate_processes" JSON field which can be either:
 *   - a plain string (e.g. "no duplicates")
 *   - an array of .tra file path strings
 */
public class DuplicateProcessesDeserializer extends StdDeserializer<List<String>> {

    public DuplicateProcessesDeserializer() {
        super(List.class);
    }

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return List.of(p.getText());
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            List<String> result = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                result.add(p.getText());
            }
            return result;
        }
        return List.of();
    }
}
