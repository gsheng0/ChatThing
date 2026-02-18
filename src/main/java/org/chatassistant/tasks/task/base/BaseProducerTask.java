package org.chatassistant.tasks.task.base;

import org.chatassistant.tasks.task.ConsumerTask;
import org.chatassistant.tasks.task.ProducerTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseProducerTask<T> extends BaseTask implements ProducerTask<T> {
    private ConcurrentHashMap<String, BlockingDeque<T>> dequeMap;

    public BaseProducerTask() {
        this.dequeMap = new ConcurrentHashMap<>();
    }

    @Override
    public final void run() {
        while (isRunning()) {
            final List<T> products = produce();
            for (final Map.Entry<String, BlockingDeque<T>> entry : dequeMap.entrySet()) {

                final BlockingDeque<T> deque = entry.getValue();
                for (T product : products) {
                    if (!shouldReceive(product, entry.getKey())) {
                        continue;
                    }
                    deque.addLast(product);
                }
            }
        }
    }

    @Override
    public final boolean register(final ConsumerTask<T> consumerTask) {
        final int size = dequeMap.size();
        dequeMap.put(computeKey(consumerTask), consumerTask.inputDeque());
        return size != dequeMap.size();
    }

    public ConcurrentHashMap<String, BlockingDeque<T>> getDequeMap() {
        return dequeMap;
    }
}
