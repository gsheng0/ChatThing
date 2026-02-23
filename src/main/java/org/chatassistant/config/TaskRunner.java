package org.chatassistant.config;

import jakarta.annotation.PostConstruct;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.ClaudeAgent;
import org.chatassistant.ai.agent.GeminiAgent;
import org.chatassistant.ai.agent.GeminiContext;

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
import java.util.Map;

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

        for (final Map.Entry<String, TasksConfigurationProperties.CapabilityConfig> entry : tasksConfig.getCapabilities().entrySet()) {
            final String capabilityName = entry.getKey();
            final TasksConfigurationProperties.CapabilityConfig cap = entry.getValue();

            final AiAgent<GeminiContext> agent = "claude".equalsIgnoreCase(cap.getProvider())
                    ? new ClaudeAgent(cap.getModelName(), cap.getPromptPath(), cap.isRealToolSet(), toolHolder)
                    : new GeminiAgent(cap.getModelName(), cap.getPromptPath(), cap.isRealToolSet(), toolHolder);
            final ChatProcessingTask chatTask = new ChatProcessingTask(agent, contextManager, capabilityName, capabilityName);
            final ConsumerRunner<Message> consumer = new ConsumerRunner<>(chatTask);

            producer.register(consumer, cap.getChats());

            Thread.ofVirtual().name("Consumer-" + capabilityName).start(consumer);
        }

        Thread.ofVirtual().name("MessagePoller").start(producer);
    }
}
