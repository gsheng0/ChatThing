package org.chatassistant.agent;

import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.agent.ClaudeAgent;
import org.chatassistant.ai.agent.GeminiAgent;
import org.chatassistant.ai.agent.AgentContext;
import org.chatassistant.ai.tools.ToolRegistry;
import org.chatassistant.context.ContextManager;
import org.chatassistant.entities.Message;
import org.chatassistant.task.runner.ConsumerRunner;
import org.chatassistant.task.runner.ProducerRunner;
import org.chatassistant.task.tasks.ChatProcessingTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentManager {
    private record RunningAgent(ConsumerRunner<Message> runner, Thread thread, List<String> chats) {}

    private final Map<String, RunningAgent> running = new ConcurrentHashMap<>();
    private final ToolRegistry toolRegistry;
    private final ContextManager contextManager;
    private final ProducerRunner<Message> producer;

    public AgentManager(final ToolRegistry toolRegistry,
                        final ContextManager contextManager,
                        final ProducerRunner<Message> producer) {
        this.toolRegistry = toolRegistry;
        this.contextManager = contextManager;
        this.producer = producer;
    }

    public void start(final String name, final AgentConfig config) {
        if (running.containsKey(name)) return;

        final AiAgent<AgentContext> agent = "claude".equalsIgnoreCase(config.getProvider())
                ? new ClaudeAgent(config.getModelName(), config.getPromptPath(), toolRegistry)
                : new GeminiAgent(config.getModelName(), config.getPromptPath(), toolRegistry);

        final ChatProcessingTask chatTask = new ChatProcessingTask(agent, contextManager, name, name);
        final ConsumerRunner<Message> consumer = new ConsumerRunner<>(chatTask);
        final List<String> chats = config.getChats() != null ? config.getChats() : List.of();
        producer.register(consumer, chats);
        final Thread thread = Thread.ofVirtual().name("Consumer-" + name).start(consumer);
        running.put(name, new RunningAgent(consumer, thread, chats));
    }

    public void stop(final String name) {
        final RunningAgent ra = running.remove(name);
        if (ra == null) return;
        ra.runner().stop();
        ra.thread().interrupt();
        producer.unregister(ra.runner(), ra.chats());
    }

    public void enable(final String name, final AgentRegistry registry) {
        final AgentConfig config = registry.getAgents().get(name);
        if (config == null) return;
        config.setEnabled(true);
        start(name, config);
    }

    public void disable(final String name, final AgentRegistry registry) {
        final AgentConfig config = registry.getAgents().get(name);
        if (config != null) config.setEnabled(false);
        stop(name);
    }

    public void update(final String name, final AgentRegistry registry) {
        stop(name);
        final AgentConfig config = registry.getAgents().get(name);
        if (config != null && config.isEnabled()) {
            start(name, config);
        }
    }

    public String listAll(final AgentRegistry registry) {
        final StringBuilder sb = new StringBuilder("Agents:\n");
        for (final Map.Entry<String, AgentConfig> entry : registry.getAgents().entrySet()) {
            final String agentName = entry.getKey();
            final AgentConfig config = entry.getValue();
            final String status = running.containsKey(agentName) ? "running" : "stopped";
            sb.append(String.format("  %s — enabled=%b, status=%s, chats=%s, model=%s%n",
                    agentName, config.isEnabled(), status, config.getChats(), config.getModelName()));
        }
        return sb.toString().trim();
    }
}
