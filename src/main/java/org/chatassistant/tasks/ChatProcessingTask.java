package org.chatassistant.tasks;

import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.entities.Message;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class ChatProcessingTask implements Runnable {
    private final BlockingDeque<Message> messageQueue;
    private volatile boolean running;
    private final String chatName;
    private final AiAgent chatAssistantAgent;
    private final AiAgent imageParserAgent;

    public ChatProcessingTask(
        final BlockingDeque<Message> messageQueue,
        final AiAgent chatAssistantAgent,
        final AiAgent imageParserAgent,
        final String chatName) {
            this.messageQueue = messageQueue;
            this.running = true;
            this.chatAssistantAgent = chatAssistantAgent;
            this.imageParserAgent = imageParserAgent;
            this.chatName = chatName;
    }

    @Override
    public void run() {
        while(running) {
            final Message message = messageQueue.pollFirst();
            final StringBuilder builder = new StringBuilder();
            final String imagePath = message.getImagePath();
            if(imagePath != null && !imagePath.isEmpty()) {
                String receiptResponse = imageParserAgent.ask("", Collections.singletonList(imagePath));
            }

        }
    }


}
