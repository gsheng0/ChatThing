package org.chatassistant.task.runner;

import org.chatassistant.task.ProcessingTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ProcessorRunner<A, B> extends Runner {
    private final BlockingDeque<A> inputQueue = new LinkedBlockingDeque<>();
    private final ConcurrentHashMap<String, List<BlockingDeque<B>>> outputMap = new ConcurrentHashMap<>();
    private final ProcessingTask<A, B> task;

    public ProcessorRunner(final ProcessingTask<A, B> task) {
        this.task = task;
    }

    public BlockingDeque<A> queue() {
        return inputQueue;
    }

    public void register(final ConsumerRunner<B> runner, final String topic) {
        outputMap.putIfAbsent(topic, new ArrayList<>());
        outputMap.get(topic).add(runner.queue());
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                task.consume(inputQueue.take());
                final List<B> products = task.produce();
                for (final B product : products) {
                    distribute(product);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void distribute(final B product) {
        for (final Map.Entry<String, List<BlockingDeque<B>>> entry : outputMap.entrySet()) {
            if (!task.shouldReceive(product, entry.getKey())) {
                continue;
            }
            entry.getValue().forEach(d -> d.addLast(product));
        }
    }
}
