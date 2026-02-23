package org.chatassistant.context.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.Content;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.GeminiContext;
import org.chatassistant.config.ContextConfigurationProperties;
import org.chatassistant.context.ContextStore;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class JsonContextStore implements ContextStore {
    private static final Logger logger = Logger.of(JsonContextStore.class);
    private static final ObjectMapper om = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private final String storagePath;

    public JsonContextStore(final ContextConfigurationProperties config) {
        this.storagePath = config.getStoragePath();
    }

    @Override
    public Optional<GeminiContext> load(final String key) {
        final Path file = filePath(key);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            final String json = Files.readString(file, StandardCharsets.UTF_8);
            final List<String> contentJsonList = om.readValue(json, LIST_TYPE);
            final GeminiContext context = new GeminiContext();
            for (final String contentJson : contentJsonList) {
                context.getHistory().add(Content.fromJson(contentJson));
            }
            return Optional.of(context);
        } catch (Exception e) {
            logger.log("Warning: failed to load context for [{}]: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(final String key, final GeminiContext context) {
        try {
            final Path file = filePath(key);
            Files.createDirectories(file.getParent());
            final List<String> contentJsonList = context.getHistory().stream()
                    .map(Content::toJson)
                    .toList();
            final String json = om.writeValueAsString(contentJsonList);
            Files.writeString(file, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.log("Warning: failed to save context for [{}]: {}", key, e.getMessage());
        }
    }

    private Path filePath(final String key) {
        return Path.of(storagePath, sanitize(key) + ".json");
    }

    private static String sanitize(final String input) {
        return input.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}
