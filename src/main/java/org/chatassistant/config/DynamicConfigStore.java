package org.chatassistant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DynamicConfigStore {
    private static final String CONFIG_PATH = "capabilities.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public DynamicConfig load() {
        final File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return new DynamicConfig();
        }
        try {
            return objectMapper.readValue(file, DynamicConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + CONFIG_PATH, e);
        }
    }

    public void save(final DynamicConfig config) {
        try {
            objectMapper.writeValue(new File(CONFIG_PATH), config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save " + CONFIG_PATH, e);
        }
    }
}
