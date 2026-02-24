package org.chatassistant.agent;

import jakarta.annotation.PostConstruct;
import org.chatassistant.Logger;
import org.chatassistant.ai.tools.ToolRegistry;
import org.chatassistant.config.LoggingConfigurationProperties;
import org.chatassistant.config.TasksConfigurationProperties;
import org.chatassistant.context.ContextManager;
import org.chatassistant.entities.Message;
import org.chatassistant.task.runner.ConsumerRunner;
import org.chatassistant.task.runner.ProducerRunner;
import org.chatassistant.task.tasks.AdminChatProcessingTask;
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
    private final LoggingConfigurationProperties loggingConfig;
    private final AgentStore agentStore;
    private final ToolRegistry toolRegistry;
    private final ContextManager contextManager;

    @Autowired
    public TaskRunner(final MessagePollingTask pollingTask,
                      final TasksConfigurationProperties tasksConfig,
                      final LoggingConfigurationProperties loggingConfig,
                      final AgentStore agentStore,
                      final ToolRegistry toolRegistry,
                      final ContextManager contextManager) {
        this.pollingTask = pollingTask;
        this.tasksConfig = tasksConfig;
        this.loggingConfig = loggingConfig;
        this.agentStore = agentStore;
        this.toolRegistry = toolRegistry;
        this.contextManager = contextManager;
    }

    @PostConstruct
    public void start() {
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Logger.init(LOG_BASE + loggingConfig.getOutputFolder() + "/" + timestamp + ".log");

        final ProducerRunner<Message> producer = new ProducerRunner<>(pollingTask);
        final AgentManager agentManager = new AgentManager(toolRegistry, contextManager, producer);

        final AgentRegistry registry = agentStore.load();

        // Start all enabled agents
        registry.getAgents().entrySet().stream()
                .filter(e -> e.getValue().isEnabled())
                .forEach(e -> agentManager.start(e.getKey(), e.getValue()));

        // Register admin chat consumer
        final String adminChat = tasksConfig.getAdminChat();
        if (adminChat != null && !adminChat.isBlank()) {
            final AdminChatProcessingTask adminTask =
                    new AdminChatProcessingTask(agentManager, agentStore, adminChat);
            final ConsumerRunner<Message> adminConsumer = new ConsumerRunner<>(adminTask);
            producer.register(adminConsumer, adminChat);
            Thread.ofVirtual().name("Consumer-admin").start(adminConsumer);
        }

        Thread.ofVirtual().name("MessagePoller").start(producer);
    }
}
