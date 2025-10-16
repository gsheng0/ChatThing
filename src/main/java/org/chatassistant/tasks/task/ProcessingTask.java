package org.chatassistant.tasks.task;

public interface ProcessingTask<A, B> extends ConsumerTask<A>, ProducerTask<B> {
}
