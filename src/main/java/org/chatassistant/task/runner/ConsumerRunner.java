package org.chatassistant.task.runner;

import org.chatassistant.task.ConsumerTask;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConsumerRunner<T> extends Runner {
    private final BlockingDeque<T> queue = new LinkedBlockingDeque<>();
    private final ConsumerTask<T> task;

    public ConsumerRunner(final ConsumerTask<T> task) {
        this.task = task;
    }

    public BlockingDeque<T> queue() {
        return queue;
    }

    public String getIdentifier() {
        return task.getIdentifier();
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                task.consume(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
