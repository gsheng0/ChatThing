package org.chatassistant.task;

public interface ProcessingTask<A, B> extends ConsumerTask<A>, ProducerTask<B> {
}
