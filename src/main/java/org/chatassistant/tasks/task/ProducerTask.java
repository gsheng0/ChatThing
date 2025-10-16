package org.chatassistant.tasks.task;

import java.util.List;

public interface ProducerTask<T> extends Task {

     boolean register(ConsumerTask<T> consumerTask);

     List<T> produce();

     default boolean shouldReceive(T product, String key) {
          return true;
     }

     default String computeKey(ConsumerTask<T> consumerTask) {
          return consumerTask.getIdentifier();
     }
}
