package org.chatassistant.task.base;

import org.chatassistant.task.ConsumerTask;

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
            try {
                consume(inputDeque.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
