package org.chatassistant.ai.agent;

import com.google.genai.types.Content;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries per-conversation state across stateless {@link AiAgent#ask} calls.
 * Create one instance per logical conversation and pass it on every call.
 *
 * <p>{@code history} holds the Gemini-native conversation turns used by {@link GeminiAgent}
 * and the context trimmer.
 *
 * <p>{@code agentState} is an opaque slot for agent-specific state (e.g. a Claude
 * {@code MessageCreateParams.Builder}). Access it via {@link #getAgentState(Class)} to get a
 * descriptive error if the stored type ever mismatches, rather than a silent ClassCastException.
 */
@Getter
public class AgentContext {
    private final List<Content> history = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @Setter
    private Object agentState;

    /**
     * Returns the agent state cast to {@code type}, or {@code null} if no state has been set.
     * Throws {@link IllegalStateException} with a descriptive message if the stored state is a
     * different type.
     */
    public <T> T getAgentState(final Class<T> type) {
        if (agentState != null && !type.isInstance(agentState)) {
            throw new IllegalStateException(
                    "Agent state type mismatch: expected " + type.getName() +
                    " but was " + agentState.getClass().getName());
        }
        return type.cast(agentState);
    }
}
