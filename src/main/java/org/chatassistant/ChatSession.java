package org.chatassistant;

import lombok.Getter;
import org.chatassistant.ai.agent.AgentContext;

@Getter
public class ChatSession {
    private final String chatName;
    private final AgentContext context = new AgentContext();

    public ChatSession(final String chatName) {
        this.chatName = chatName;
    }

}
