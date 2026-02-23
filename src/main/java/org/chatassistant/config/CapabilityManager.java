package org.chatassistant.config;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CapabilityManager {
    private record RunningCapability(ConsumerRunner<Message> runner, Thread thread, String chat) {}

    private final Map<String, RunningCapability> running = new ConcurrentHashMap<>();
    private final ToolHolder toolHolder;
    private final ContextManager contextManager;
    private ProducerRunner<Message> producer;

    @Autowired
    public CapabilityManager(final ToolHolder toolHolder, final ContextManager contextManager) {
        this.toolHolder = toolHolder;
        this.contextManager = contextManager;
    }

    public void init(final ProducerRunner<Message> producer) {
        this.producer = producer;
    }

    public void start(final String name, final DynamicConfig.Capability cap) {
        if (running.containsKey(name)) return;

        final AiAgent<GeminiContext> agent = "claude".equalsIgnoreCase(cap.getProvider())
                ? new ClaudeAgent(cap.getModelName(), cap.getPromptPath(), cap.isRealToolSet(), toolHolder)
                : new GeminiAgent(cap.getModelName(), cap.getPromptPath(), cap.isRealToolSet(), toolHolder);

        final ChatProcessingTask chatTask = new ChatProcessingTask(agent, contextManager, name, name);
        final ConsumerRunner<Message> consumer = new ConsumerRunner<>(chatTask);
        producer.register(consumer, cap.getChat());
        final Thread thread = Thread.ofVirtual().name("Consumer-" + name).start(consumer);
        running.put(name, new RunningCapability(consumer, thread, cap.getChat()));
    }

    public void stop(final String name) {
        final RunningCapability rc = running.remove(name);
        if (rc == null) return;
        rc.runner().stop();
        rc.thread().interrupt();
        producer.unregister(rc.runner(), rc.chat());
    }

    public void enable(final String name, final DynamicConfig config) {
        final DynamicConfig.Capability cap = config.getCapabilities().get(name);
        if (cap == null) return;
        cap.setEnabled(true);
        start(name, cap);
    }

    public void disable(final String name, final DynamicConfig config) {
        final DynamicConfig.Capability cap = config.getCapabilities().get(name);
        if (cap != null) cap.setEnabled(false);
        stop(name);
    }

    public void update(final String name, final DynamicConfig config) {
        stop(name);
        final DynamicConfig.Capability cap = config.getCapabilities().get(name);
        if (cap != null && cap.isEnabled()) {
            start(name, cap);
        }
    }

    public String listAll(final DynamicConfig config) {
        final StringBuilder sb = new StringBuilder("Capabilities:\n");
        for (final Map.Entry<String, DynamicConfig.Capability> entry : config.getCapabilities().entrySet()) {
            final String capName = entry.getKey();
            final DynamicConfig.Capability cap = entry.getValue();
            final String status = running.containsKey(capName) ? "running" : "stopped";
            sb.append(String.format("  %s — enabled=%b, status=%s, chat=%s, model=%s%n",
                    capName, cap.isEnabled(), status, cap.getChat(), cap.getModelName()));
        }
        return sb.toString().trim();
    }
}
