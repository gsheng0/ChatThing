package org.chatassistant.task.tasks;

import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.AgentContext;
import org.chatassistant.context.ContextManager;
import org.chatassistant.entities.Message;
import org.chatassistant.task.ConsumerTask;

import java.util.ArrayList;
import java.util.List;

public class ChatProcessingTask implements ConsumerTask<Message> {
    private final AiAgent<AgentContext> agent;
    private final ContextManager contextManager;
    private final String capabilityName;
    private final String contextKey;

    public ChatProcessingTask(final AiAgent<AgentContext> agent, final ContextManager contextManager,
                              final String capabilityName, final String contextKey) {
        this.agent = agent;
        this.contextManager = contextManager;
        this.capabilityName = capabilityName;
        this.contextKey = contextKey;
    }

    @Override
    public void consume(final Message message) {
        final StringBuilder prompt = new StringBuilder();
        final List<String> imagePaths = new ArrayList<>();

        final String imagePath = message.getImagePath();
        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("None")) {
            imagePaths.add(imagePath);
        }
        if (message.getText() != null && !message.getText().isEmpty()) {
            prompt.append("[Chat: ").append(message.getChatName()).append("]\n");
            prompt.append(message.getSender()).append(": ").append(message.getText()).append("\n");
        }

        if (!prompt.isEmpty() || !imagePaths.isEmpty()) {
            final AgentContext context = contextManager.getOrCreate(contextKey);
            try {
                agent.ask(context, prompt.toString(), imagePaths);
            } catch (Exception e) {
                System.err.println("[" + capabilityName + "] agent.ask() failed: " + e.getMessage());
                e.printStackTrace();
            }
            contextManager.afterTurn(context, contextKey);
        }
    }

    @Override
    public String getIdentifier() {
        return capabilityName;
    }
}
