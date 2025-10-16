package org.chatassistant.tasks.task.base;

import org.chatassistant.tasks.task.ConsumerTask;
import org.chatassistant.tasks.task.ProcessingTask;

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
            while (!inputDeque().isEmpty()) {
                consume(inputDeque().pollFirst());
            }

            final List<B> products = produce();
            for (final BlockingDeque<B> deque : outputDeques) {
                for (final B product : products) {
                    deque.addLast(product);
                }
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
