package org.chatassistant.config;

import jakarta.annotation.PostConstruct;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.GeminiAgent;
import org.chatassistant.ai.tools.ToolHolder;
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
    private final TasksConfigurationProperties tasksConfig;
    private final ToolHolder toolHolder;
    private final ContextManager contextManager;
    private final LoggingConfigurationProperties loggingConfig;

    @Autowired
    public TaskRunner(final MessagePollingTask pollingTask,
                      final TasksConfigurationProperties tasksConfig,
                      final ToolHolder toolHolder,
                      final ContextManager contextManager,
                      final LoggingConfigurationProperties loggingConfig) {
        this.pollingTask = pollingTask;
        this.tasksConfig = tasksConfig;
        this.toolHolder = toolHolder;
        this.contextManager = contextManager;
        this.loggingConfig = loggingConfig;
    }

    @PostConstruct
    public void start() {
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Logger.init(LOG_BASE + loggingConfig.getOutputFolder() + "/" + timestamp + ".log");

        final ProducerRunner<Message> producer = new ProducerRunner<>(pollingTask);

        for (final TasksConfigurationProperties.ChatConfig chatConfig : tasksConfig.getChats()) {
            for (final String capabilityName : chatConfig.getCapabilities()) {
                final TasksConfigurationProperties.CapabilityConfig cap = tasksConfig.getCapabilities().get(capabilityName);
                if (cap == null) {
                    throw new IllegalArgumentException("Unknown capability: " + capabilityName);
                }

                final GeminiAgent agent = new GeminiAgent(cap.getModelName(), cap.getPromptPath(), cap.isRealToolSet(), toolHolder);
                final ChatProcessingTask chatTask = new ChatProcessingTask(agent, contextManager, capabilityName);
                final ConsumerRunner<Message> consumer = new ConsumerRunner<>(chatTask);

                producer.register(consumer, chatConfig.getName());

                final String threadName = "Consumer-" + chatConfig.getName() + "-" + capabilityName;
                Thread.ofVirtual().name(threadName).start(consumer);
            }
        }

        Thread.ofVirtual().name("MessagePoller").start(producer);
    }
}
