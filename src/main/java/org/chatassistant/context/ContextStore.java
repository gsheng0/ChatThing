package org.chatassistant.context;

import org.chatassistant.ai.agent.GeminiContext;

import java.util.Optional;

public interface ContextStore {
    Optional<GeminiContext> load(String chat, String capability);
    void save(String chat, String capability, GeminiContext context);
}
