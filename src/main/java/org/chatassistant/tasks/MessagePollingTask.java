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
        return MessagePoller.getInstance().getRecentMessages();
    }

    @Override
    public boolean shouldReceive(final Message message, final String key) {
        return key.equals(message.getChatName());
    }

    @Override
    public String computeKey(final ConsumerTask<Message> consumerTask) {
        return consumerTask.getIdentifier();
    }
}
