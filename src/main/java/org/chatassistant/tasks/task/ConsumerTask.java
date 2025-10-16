package org.chatassistant.tasks.task;

import java.util.concurrent.BlockingDeque;

public interface ConsumerTask<T> extends Task {

    BlockingDeque<T> inputDeque();

    void consume(T t);

    String getIdentifier();
}
