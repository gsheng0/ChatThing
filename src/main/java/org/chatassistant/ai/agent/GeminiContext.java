package org.chatassistant.ai.agent;

import com.google.genai.types.Content;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the conversation history for a stateless {@link GeminiAgent} call.
 * Create one instance per logical conversation and pass it on each {@code ask()} call
 * so that the agent can see prior turns without owning any state itself.
 */
@Getter
public class GeminiContext {
    private final List<Content> history = new ArrayList<>();
}
