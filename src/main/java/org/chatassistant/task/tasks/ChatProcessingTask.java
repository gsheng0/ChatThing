package org.chatassistant.task.tasks;

import org.chatassistant.Logger;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.GeminiContext;
import org.chatassistant.context.ContextManager;
import org.chatassistant.entities.Message;
import org.chatassistant.task.ConsumerTask;

import java.util.ArrayList;
import java.util.List;

public class ChatProcessingTask implements ConsumerTask<Message> {
    private static final Logger logger = Logger.of(ChatProcessingTask.class);

    private final AiAgent<GeminiContext> agent;
    private final ContextManager contextManager;
    private final String capabilityName;

    public ChatProcessingTask(final AiAgent<GeminiContext> agent, final ContextManager contextManager,
                              final String capabilityName) {
        this.agent = agent;
        this.contextManager = contextManager;
        this.capabilityName = capabilityName;
    }

    @Override
    public void consume(final Message message) {
        final String chatName = message.getChatName();
        final StringBuilder prompt = new StringBuilder();
        final List<String> imagePaths = new ArrayList<>();

        final String imagePath = message.getImagePath();
        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("None")) {
            imagePaths.add(imagePath);
        }
        if (message.getText() != null && !message.getText().isEmpty()) {
            prompt.append(message.getSender()).append(": ").append(message.getText()).append("\n");
        }

        if (!prompt.isEmpty() || !imagePaths.isEmpty()) {
            logger.log("[{}][{}] Processing: {}", chatName, capabilityName, prompt.toString().trim());
            final GeminiContext context = contextManager.getOrCreate(chatName, capabilityName);
            final String response = agent.ask(context, prompt.toString(), imagePaths);
            contextManager.afterTurn(context, chatName, capabilityName);
            logger.log("[{}][{}] Response: {}", chatName, capabilityName, response);
        }
    }

    @Override
    public String getIdentifier() {
        return capabilityName;
    }
}
