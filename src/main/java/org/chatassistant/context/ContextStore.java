package org.chatassistant.context;

import org.chatassistant.ai.agent.GeminiContext;

import java.util.Optional;

public interface ContextStore {
    Optional<GeminiContext> load(String key);
    void save(String key, GeminiContext context);
}
