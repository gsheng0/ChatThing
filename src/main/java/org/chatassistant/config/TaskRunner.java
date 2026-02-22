package org.chatassistant.config;

import jakarta.annotation.PostConstruct;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.GeminiContext;
import org.chatassistant.context.ContextManager;
import org.chatassistant.entities.Message;
import org.chatassistant.task.runner.ConsumerRunner;
import org.chatassistant.task.runner.ProducerRunner;
import org.chatassistant.task.tasks.ChatProcessingTask;
import org.chatassistant.task.tasks.MessagePollingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TaskRunner {
    private static final String LOG_BASE = "/Users/georgesheng/proj/scheduler2/logs/";

    private final MessagePollingTask pollingTask;
    private final AiAgent<GeminiContext> agent;
    private final ContextManager contextManager;
    private final LoggingConfigurationProperties loggingConfig;

    @Autowired
    public TaskRunner(final MessagePollingTask pollingTask, final AiAgent<GeminiContext> agent,
                      final ContextManager contextManager, final LoggingConfigurationProperties loggingConfig) {
        this.pollingTask = pollingTask;
        this.agent = agent;
        this.contextManager = contextManager;
        this.loggingConfig = loggingConfig;
    }

    @PostConstruct
    public void start() {
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Logger.init(LOG_BASE + loggingConfig.getOutputFolder() + "/" + timestamp + ".log");

        final ProducerRunner<Message> producer = new ProducerRunner<>(pollingTask);

        final ChatProcessingTask chatTask = new ChatProcessingTask(agent, contextManager, "expenses");
        final ConsumerRunner<Message> consumer = new ConsumerRunner<>(chatTask);
        producer.register(consumer, consumer.getIdentifier());

        Thread.ofVirtual().name("MessagePoller").start(producer);
        Thread.ofVirtual().name("ChatProcessor").start(consumer);
    }
}
