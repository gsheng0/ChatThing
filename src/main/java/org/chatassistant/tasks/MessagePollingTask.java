package org.chatassistant.tasks;

import org.chatassistant.data.MessagePoller;
import org.chatassistant.entities.Message;
import org.chatassistant.tasks.task.ConsumerTask;
import org.chatassistant.tasks.task.base.BaseProducerTask;

import java.util.List;

public class MessagePollingTask extends BaseProducerTask<Message> {
    public MessagePollingTask(){
        super();
    }

    @Override
    public List<Message> produce() {
        final List<Message> messages = MessagePoller.getInstance().getRecentMessages();
        for (final Message message : messages) {
            final String chatName = message.getChatName();
            if (!getDequeMap().containsKey(chatName)) {
                final ConsumerTask<Message> consumerTask = new ChatProcessingTask(null, null, chatName);
                final Thread thread = new Thread(consumerTask);
                thread.start();
                register(consumerTask);
            }

            getDequeMap().get(chatName).add(message);
        }
        return messages;
    }

    @Override
    public boolean shouldReceive(final Message message, final String key) {
        return key.equals(message.getChatName());
    }
}
