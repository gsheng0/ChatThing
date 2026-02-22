package org.chatassistant.task;

public interface ConsumerTask<T> {
    void consume(T item);
    String getIdentifier();
}
