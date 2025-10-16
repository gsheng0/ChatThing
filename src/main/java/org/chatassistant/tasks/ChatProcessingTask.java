package org.chatassistant.tasks;

import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.entities.Message;
import org.chatassistant.tasks.task.base.BaseConsumerTask;

import java.util.Collections;

public class ChatProcessingTask extends BaseConsumerTask<Message> {
    private final String chatName;
    private final AiAgent chatAssistantAgent;
    private final AiAgent imageParserAgent;

    public ChatProcessingTask(
            final AiAgent chatAssistantAgent,
            final AiAgent imageParserAgent,
            final String chatName) {
        super();
        this.chatAssistantAgent = chatAssistantAgent;
        this.imageParserAgent = imageParserAgent;
        this.chatName = chatName;
    }

    @Override
    public void consume(final Message message) {
        final StringBuilder builder = new StringBuilder();
        final String imagePath = message.getImagePath();
        if(imagePath != null && !imagePath.isEmpty()) {
            String receiptResponse = imageParserAgent.ask("", Collections.singletonList(imagePath));
        }
    }

    @Override
    public String getIdentifier() {
        return chatName;
    }
}
