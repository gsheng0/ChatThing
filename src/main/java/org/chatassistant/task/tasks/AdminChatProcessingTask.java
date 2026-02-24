package org.chatassistant.task.tasks;

import org.chatassistant.agent.AgentConfig;
import org.chatassistant.agent.AgentManager;
import org.chatassistant.agent.AgentRegistry;
import org.chatassistant.agent.AgentStore;
import org.chatassistant.ai.agent.ClaudeAgent;
import org.chatassistant.ai.agent.AgentContext;
import org.chatassistant.entities.Message;
import org.chatassistant.task.ConsumerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AdminChatProcessingTask implements ConsumerTask<Message> {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final String IDENTIFIER = "admin";

    private static final String HELP = """
            Available commands:
              list
              add <name> <chat name>
              remove <name>
              enable <name>
              disable <name>
              set <name> chat <chat name>
              set <name> prompt <path>
              set <name> model <model>
              fix <name> <problem description>""";

    private final AgentManager agentManager;
    private final AgentStore agentStore;
    private final String adminChat;

    public AdminChatProcessingTask(final AgentManager agentManager,
                                   final AgentStore agentStore,
                                   final String adminChat) {
        this.agentManager = agentManager;
        this.agentStore = agentStore;
        this.adminChat = adminChat;
    }

    @Override
    public void consume(final Message message) {
        final String text = message.getText();
        if (text == null || text.isBlank()) return;

        final String trimmed = text.trim();
        final String reply = handleCommand(trimmed);
        sendReply(reply);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    private String handleCommand(final String input) {
        final String[] parts = input.split("\\s+", 2);
        final String cmd = parts[0].toLowerCase();
        final String rest = parts.length > 1 ? parts[1] : "";

        try {
            return switch (cmd) {
                case "list" -> handleList();
                case "add" -> handleAdd(rest);
                case "remove" -> handleRemove(rest);
                case "enable" -> handleEnable(rest);
                case "disable" -> handleDisable(rest);
                case "set" -> handleSet(rest);
                case "fix" -> handleFix(rest);
                default -> HELP;
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleList() {
        final AgentRegistry registry = agentStore.load();
        return agentManager.listAll(registry);
    }

    private String handleAdd(final String rest) {
        // add <name> <chat name>
        final String[] parts = rest.split("\\s+", 2);
        if (parts.length < 2) return "Usage: add <name> <chat name>";

        final String name = parts[0];
        final String chat = parts[1];

        final AgentRegistry registry = agentStore.load();
        if (registry.getAgents().containsKey(name)) {
            return "Agent \'" + name + "\' already exists.";
        }

        final AgentConfig config = new AgentConfig();
        config.setProvider("claude");
        config.setModelName("claude-sonnet-4-6");
        config.setPromptPath("src/main/resources/expenseTrackingPrompt2");
        config.setChats(List.of(chat));
        config.setEnabled(true);

        registry.getAgents().put(name, config);
        agentStore.save(registry);
        agentManager.start(name, config);
        return "Added and started agent \'" + name + "\' on chat \'" + chat + "\'.";
    }

    private String handleRemove(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: remove <name>";

        if (agentStore.isYamlDefined(name)) {
            return "Agent '" + name + "' is defined in application.yaml and cannot be removed. Use 'disable " + name + "' instead.";
        }

        final AgentRegistry registry = agentStore.load();
        if (!registry.getAgents().containsKey(name)) {
            return "Agent '" + name + "' not found.";
        }

        agentManager.stop(name);
        registry.getAgents().remove(name);
        agentStore.save(registry);
        return "Removed agent '" + name + "'.";
    }

    private String handleEnable(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: enable <name>";

        final AgentRegistry registry = agentStore.load();
        if (!registry.getAgents().containsKey(name)) {
            return "Agent \'" + name + "\' not found.";
        }

        agentManager.enable(name, registry);
        agentStore.save(registry);
        return "Enabled agent \'" + name + "\'.";
    }

    private String handleDisable(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: disable <name>";

        final AgentRegistry registry = agentStore.load();
        if (!registry.getAgents().containsKey(name)) {
            return "Agent \'" + name + "\' not found.";
        }

        agentManager.disable(name, registry);
        agentStore.save(registry);
        return "Disabled agent \'" + name + "\'.";
    }

    private String handleSet(final String rest) {
        // set <name> <field> <value>
        final String[] parts = rest.split("\\s+", 3);
        if (parts.length < 3) return "Usage: set <name> chat|prompt|model <value>";

        final String name = parts[0];
        final String field = parts[1].toLowerCase();
        final String value = parts[2];

        final AgentRegistry registry = agentStore.load();
        final AgentConfig config = registry.getAgents().get(name);
        if (config == null) return "Agent \'" + name + "\' not found.";

        switch (field) {
            case "chat" -> config.setChats(List.of(value));
            case "prompt" -> config.setPromptPath(value);
            case "model" -> config.setModelName(value);
            default -> { return "Unknown field \'" + field + "\'. Use: chat, prompt, or model"; }
        }

        agentStore.save(registry);
        agentManager.update(name, registry);
        return "Updated " + field + " for \'" + name + "\' to: " + value;
    }

    private String handleFix(final String rest) {
        // fix <name> <problem description>
        final String[] parts = rest.split("\\s+", 2);
        if (parts.length < 2) return "Usage: fix <name> <problem description>";

        final String name = parts[0];
        final String problem = parts[1];

        final AgentRegistry registry = agentStore.load();
        final AgentConfig config = registry.getAgents().get(name);
        if (config == null) return "Agent \'" + name + "\' not found.";

        final String promptPath = config.getPromptPath();
        final String currentPrompt;
        try {
            currentPrompt = Files.readString(Path.of(promptPath));
        } catch (Exception e) {
            return "Error reading prompt at " + promptPath + ": " + e.getMessage();
        }

        final String updatedPrompt;
        try {
            updatedPrompt = rewritePrompt(currentPrompt, problem);
        } catch (Exception e) {
            return "Error calling LLM: " + e.getMessage();
        }

        try {
            Files.writeString(Path.of(promptPath), updatedPrompt);
        } catch (Exception e) {
            return "Error writing updated prompt: " + e.getMessage();
        }

        agentManager.update(name, registry);
        return "Prompt for \'" + name + "\' updated and agent restarted.";
    }

    private String rewritePrompt(final String currentPrompt, final String problem) {
        final ClaudeAgent agent = ClaudeAgent.toolFree("claude-sonnet-4-6",
                "You are an expert AI prompt engineer. You will be given an AI agent's " +
                "system prompt and a description of a problem or desired improvement. " +
                "Rewrite the prompt to address it. Return ONLY the updated prompt text — " +
                "no explanation, no preamble, no markdown code fences.");
        final String result = agent.ask(new AgentContext(),
                "Current prompt:\n\n" + currentPrompt + "\n\n---\nProblem to fix: " + problem);
        agent.kill();
        if (result == null) throw new RuntimeException("no text in response");
        return result;
    }

    private static final int MAX_MESSAGE_LENGTH = 2000;

    private void sendReply(final String message) {
        final String safeMessage = message.length() > MAX_MESSAGE_LENGTH
                ? message.substring(0, MAX_MESSAGE_LENGTH) + "…"
                : message;

        final Process process;
        try {
            process = new ProcessBuilder("osascript", SCRIPT_PATH, adminChat,
                    "[Admin]: " + safeMessage).start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = error.readLine()) != null) {
                System.err.println("[AdminReply Error] " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            process.destroy();
        }
    }
}
