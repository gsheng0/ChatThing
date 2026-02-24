package org.chatassistant.context;

import org.chatassistant.ai.agent.AgentContext;

import java.util.Optional;

public interface ContextStore {
    Optional<AgentContext> load(String key);
    void save(String key, AgentContext context);
}
