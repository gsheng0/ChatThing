package org.chatassistant.task.base;

import org.chatassistant.task.ConsumerTask;
import org.chatassistant.task.ProcessingTask;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class BaseProcessorTask<A, B> extends BaseTask implements ProcessingTask<A, B> {
    private final ConcurrentLinkedQueue<BlockingDeque<B>>  outputDeques;
    private final BlockingDeque<A> inputDeque;

    public BaseProcessorTask() {
        outputDeques = new ConcurrentLinkedQueue<>();
        inputDeque = new LinkedBlockingDeque<>();
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                final A item = inputDeque().take();
                consume(item);
                final List<B> products = produce();
                for (final BlockingDeque<B> deque : outputDeques) {
                    for (final B product : products) {
                        deque.addLast(product);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public BlockingDeque<A> inputDeque() {
        return inputDeque;
    }

    @Override
    public boolean register(final ConsumerTask<B> consumerTask) {
        return outputDeques.add(consumerTask.inputDeque());
    }
}
