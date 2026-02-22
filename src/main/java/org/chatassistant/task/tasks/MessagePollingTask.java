package org.chatassistant.task.tasks;

import org.chatassistant.data.MessagePoller;
import org.chatassistant.entities.Message;
import org.chatassistant.task.ConsumerTask;
import org.chatassistant.task.ProducerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MessagePollingTask implements ProducerTask<Message> {
    private static final int PASSIVE = 0;
    private static final int ACTIVE = 1;

    private final Set<Message> seenMessages;
    private final MessagePoller messagePoller;

    private int status = PASSIVE;
    private int activeCounter = 0;

    @Autowired
    public MessagePollingTask(final MessagePoller messagePoller) {
        this.messagePoller = messagePoller;
        this.seenMessages = new HashSet<>(messagePoller.getRecentMessages());
    }

    @Override
    public List<Message> produce() {
        final List<Message> recent = messagePoller.getRecentMessages();
        final List<Message> newMessages = new ArrayList<>();

        for (final Message message : recent) {
            if (seenMessages.contains(message) || message.getText().startsWith("[Intern]:")) {
                continue;
            }
            newMessages.add(message);
            seenMessages.add(message);
        }

        try {
            if (!newMessages.isEmpty()) {
                status = ACTIVE;
                activeCounter = 5;
            } else if (--activeCounter <= 0) {
                status = PASSIVE;
            }
            Thread.sleep(status == ACTIVE ? 1000L : 5000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return newMessages;
    }

}
