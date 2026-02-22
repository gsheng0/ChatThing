package org.chatassistant;

import lombok.Getter;
import org.chatassistant.ai.agent.GeminiContext;

@Getter
public class ChatSession {
    private final String chatName;
    private final GeminiContext context = new GeminiContext();

    public ChatSession(final String chatName) {
        this.chatName = chatName;
    }

}
