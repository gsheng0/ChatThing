package org.chatassistant.context;

import org.chatassistant.ai.agent.GeminiContext;

import java.util.Optional;

public interface ContextTrimmer {
    // Returns summary text if trimming occurred; Optional.empty() if within limits
    Optional<String> trim(GeminiContext context);
}
