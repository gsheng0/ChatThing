package org.chatassistant.tasks.task.base;

import org.chatassistant.tasks.task.ConsumerTask;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class BaseConsumerTask<T> extends BaseTask implements ConsumerTask<T> {
    private final BlockingDeque<T> inputDeque;

    public BaseConsumerTask() {
        inputDeque = new LinkedBlockingDeque<>();
    }

    @Override
    public final BlockingDeque<T> inputDeque() {
        return inputDeque;
    }

    @Override
    public final void run() {
        while (isRunning()) {
            while (!inputDeque.isEmpty()) {
                consume(inputDeque.pollFirst());
            }
        }
    }
}
