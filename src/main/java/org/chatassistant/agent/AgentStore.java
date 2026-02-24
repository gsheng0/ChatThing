package org.chatassistant.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.chatassistant.config.AgentsConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages agent configuration persistence.
 *
 * YAML ({@code application.yaml}) is the permanent source of truth for statically configured
 * agents. {@code agents.json} stores only runtime overrides — agents added or modified via admin
 * commands. On every {@link #load()}, the two layers are merged: YAML provides the base, JSON
 * overrides on top. On every {@link #save}, only the diff vs YAML is persisted, so the JSON
 * stays minimal and YAML changes always take effect for unmodified agents.
 *
 * YAML-defined agents cannot be permanently deleted via admin commands (only disabled), so no
 * tombstone logic is needed.
 */
@Component
public class AgentStore {
    private static final String CONFIG_PATH = "agents.json";

    private final Map<String, AgentConfig> yamlDefaults;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Autowired
    public AgentStore(final AgentsConfigurationProperties tasksConfig) {
        this.yamlDefaults = tasksConfig.getAgents();
    }

    /**
     * Returns the effective agent registry: YAML agents as the base, with any runtime overrides
     * from agents.json layered on top. If agents.json does not exist, returns YAML agents only.
     */
    public synchronized AgentRegistry load() {
        final Map<String, AgentConfig> effective = new LinkedHashMap<>(yamlDefaults);
        final File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try {
                final AgentRegistry overrides = objectMapper.readValue(file, AgentRegistry.class);
                effective.putAll(overrides.getAgents());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load " + CONFIG_PATH, e);
            }
        }
        final AgentRegistry registry = new AgentRegistry();
        registry.setAgents(effective);
        return registry;
    }

    /**
     * Persists only the diff between the given registry and the YAML baseline. Agents identical
     * to their YAML counterpart are not written, keeping agents.json minimal.
     */
    public synchronized void save(final AgentRegistry registry) {
        final Map<String, AgentConfig> overrides = new LinkedHashMap<>();
        for (final Map.Entry<String, AgentConfig> entry : registry.getAgents().entrySet()) {
            final AgentConfig yaml = yamlDefaults.get(entry.getKey());
            if (yaml == null || !entry.getValue().equals(yaml)) {
                overrides.put(entry.getKey(), entry.getValue());
            }
        }
        final AgentRegistry overridesRegistry = new AgentRegistry();
        overridesRegistry.setAgents(overrides);
        try {
            objectMapper.writeValue(new File(CONFIG_PATH), overridesRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save " + CONFIG_PATH, e);
        }
    }

    /** Returns true if this agent name is defined in YAML (and therefore cannot be removed). */
    public boolean isYamlDefined(final String name) {
        return yamlDefaults.containsKey(name);
    }
}
