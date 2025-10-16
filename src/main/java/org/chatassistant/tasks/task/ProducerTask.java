package org.chatassistant.tasks.task;

import java.util.List;

public interface ProducerTask<T> extends Task {
     boolean register(ConsumerTask<T> consumerTask);
     List<T> produce();
     boolean shouldReceive(T product, String key);
     String computeKey(ConsumerTask<T> consumerTask);
}
