package org.chatassistant.task;

import java.util.List;

public interface ProducerTask<T> {
    List<T> produce();

    default boolean shouldReceive(T product, String consumerKey) { return true; }

    default String extractKey(T product) { return null; }

    default ConsumerTask<T> createConsumerFor(String consumerKey) { return null; }
}
