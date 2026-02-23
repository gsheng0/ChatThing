package org.chatassistant.config;

import jakarta.annotation.PostConstruct;
import org.chatassistant.Logger;
import org.chatassistant.entities.Message;
import org.chatassistant.task.runner.ConsumerRunner;
import org.chatassistant.task.runner.ProducerRunner;
import org.chatassistant.task.tasks.AdminChatProcessingTask;
import org.chatassistant.task.tasks.MessagePollingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class TaskRunner {
    private static final String LOG_BASE = "/Users/georgesheng/proj/scheduler2/logs/";

    private final MessagePollingTask pollingTask;
    private final TasksConfigurationProperties tasksConfig;
    private final LoggingConfigurationProperties loggingConfig;
    private final CapabilityManager capabilityManager;
    private final DynamicConfigStore configStore;

    @Autowired
    public TaskRunner(final MessagePollingTask pollingTask,
                      final TasksConfigurationProperties tasksConfig,
                      final LoggingConfigurationProperties loggingConfig,
                      final CapabilityManager capabilityManager,
                      final DynamicConfigStore configStore) {
        this.pollingTask = pollingTask;
        this.tasksConfig = tasksConfig;
        this.loggingConfig = loggingConfig;
        this.capabilityManager = capabilityManager;
        this.configStore = configStore;
    }

    @PostConstruct
    public void start() {
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Logger.init(LOG_BASE + loggingConfig.getOutputFolder() + "/" + timestamp + ".log");

        final ProducerRunner<Message> producer = new ProducerRunner<>(pollingTask);
        capabilityManager.init(producer);

        // Bootstrap: load JSON; seed from YAML capabilities if absent
        DynamicConfig config = configStore.load();
        if (config.getCapabilities().isEmpty() && !tasksConfig.getCapabilities().isEmpty()) {
            for (final var entry : tasksConfig.getCapabilities().entrySet()) {
                final TasksConfigurationProperties.CapabilityConfig src = entry.getValue();
                final DynamicConfig.Capability cap = new DynamicConfig.Capability();
                cap.setProvider(src.getProvider());
                cap.setPromptPath(src.getPromptPath());
                cap.setModelName(src.getModelName());
                cap.setRealToolSet(src.isRealToolSet());
                // Use first chat in the list as the single chat value
                final List<String> chats = src.getChats();
                cap.setChat(chats != null && !chats.isEmpty() ? chats.get(0) : null);
                cap.setEnabled(true);
                config.getCapabilities().put(entry.getKey(), cap);
            }
            configStore.save(config);
        }

        // Start all enabled capabilities
        config.getCapabilities().entrySet().stream()
                .filter(e -> e.getValue().isEnabled())
                .forEach(e -> capabilityManager.start(e.getKey(), e.getValue()));

        // Register admin chat consumer
        final String adminChat = tasksConfig.getAdminChat();
        if (adminChat != null && !adminChat.isBlank()) {
            final AdminChatProcessingTask adminTask =
                    new AdminChatProcessingTask(capabilityManager, configStore, adminChat);
            final ConsumerRunner<Message> adminConsumer = new ConsumerRunner<>(adminTask);
            producer.register(adminConsumer, adminChat);
            Thread.ofVirtual().name("Consumer-admin").start(adminConsumer);
        }

        Thread.ofVirtual().name("MessagePoller").start(producer);
    }
}
