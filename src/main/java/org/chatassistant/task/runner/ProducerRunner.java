package org.chatassistant.task.runner;

import org.chatassistant.task.ProducerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

public class ProducerRunner<T> extends Runner {
    private final ConcurrentHashMap<String, List<BlockingDeque<T>>> outputMap = new ConcurrentHashMap<>();
    private final ProducerTask<T> task;

    public ProducerRunner(final ProducerTask<T> task) {
        this.task = task;
    }

    public void register(final ConsumerRunner<T> runner, final String topic) {
        outputMap.putIfAbsent(topic, new ArrayList<>());
        outputMap.get(topic).add(runner.queue());
    }

    public void register(final ConsumerRunner<T> runner, final List<String> topics) {
        for (String topic : topics) {
            register(runner, topic);
        }
    }

    @Override
    public void run() {
        while (isRunning()) {
            task.produce().forEach(this::distribute);
        }
    }

    private void distribute(final T product) {
        for (final Map.Entry<String, List<BlockingDeque<T>>> entry : outputMap.entrySet()) {
            if (!task.shouldReceive(product, entry.getKey())) {
                continue;
            }
            entry.getValue().forEach(d -> d.addLast(product));
        }
    }
}
