package org.chatassistant.task.tasks;

import org.chatassistant.config.CapabilityManager;
import org.chatassistant.config.DynamicConfig;
import org.chatassistant.config.DynamicConfigStore;
import org.chatassistant.entities.Message;
import org.chatassistant.task.ConsumerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
              set <name> model <model>""";

    private final CapabilityManager capabilityManager;
    private final DynamicConfigStore configStore;
    private final String adminChat;

    public AdminChatProcessingTask(final CapabilityManager capabilityManager,
                                   final DynamicConfigStore configStore,
                                   final String adminChat) {
        this.capabilityManager = capabilityManager;
        this.configStore = configStore;
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
                default -> HELP;
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String handleList() {
        final DynamicConfig config = configStore.load();
        return capabilityManager.listAll(config);
    }

    private String handleAdd(final String rest) {
        // add <name> <chat name>
        final String[] parts = rest.split("\\s+", 2);
        if (parts.length < 2) return "Usage: add <name> <chat name>";

        final String name = parts[0];
        final String chat = parts[1];

        final DynamicConfig config = configStore.load();
        if (config.getCapabilities().containsKey(name)) {
            return "Capability '" + name + "' already exists.";
        }

        final DynamicConfig.Capability cap = new DynamicConfig.Capability();
        cap.setProvider("claude");
        cap.setModelName("claude-sonnet-4-6");
        cap.setPromptPath("src/main/resources/expenseTrackingPrompt2");
        cap.setRealToolSet(true);
        cap.setChat(chat);
        cap.setEnabled(true);

        config.getCapabilities().put(name, cap);
        configStore.save(config);
        capabilityManager.start(name, cap);
        return "Added and started capability '" + name + "' on chat '" + chat + "'.";
    }

    private String handleRemove(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: remove <name>";

        final DynamicConfig config = configStore.load();
        if (!config.getCapabilities().containsKey(name)) {
            return "Capability '" + name + "' not found.";
        }

        capabilityManager.stop(name);
        config.getCapabilities().remove(name);
        configStore.save(config);
        return "Removed capability '" + name + "'.";
    }

    private String handleEnable(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: enable <name>";

        final DynamicConfig config = configStore.load();
        if (!config.getCapabilities().containsKey(name)) {
            return "Capability '" + name + "' not found.";
        }

        capabilityManager.enable(name, config);
        configStore.save(config);
        return "Enabled capability '" + name + "'.";
    }

    private String handleDisable(final String rest) {
        final String name = rest.trim();
        if (name.isEmpty()) return "Usage: disable <name>";

        final DynamicConfig config = configStore.load();
        if (!config.getCapabilities().containsKey(name)) {
            return "Capability '" + name + "' not found.";
        }

        capabilityManager.disable(name, config);
        configStore.save(config);
        return "Disabled capability '" + name + "'.";
    }

    private String handleSet(final String rest) {
        // set <name> <field> <value>
        final String[] parts = rest.split("\\s+", 3);
        if (parts.length < 3) return "Usage: set <name> chat|prompt|model <value>";

        final String name = parts[0];
        final String field = parts[1].toLowerCase();
        final String value = parts[2];

        final DynamicConfig config = configStore.load();
        final DynamicConfig.Capability cap = config.getCapabilities().get(name);
        if (cap == null) return "Capability '" + name + "' not found.";

        switch (field) {
            case "chat" -> cap.setChat(value);
            case "prompt" -> cap.setPromptPath(value);
            case "model" -> cap.setModelName(value);
            default -> { return "Unknown field '" + field + "'. Use: chat, prompt, or model"; }
        }

        configStore.save(config);
        capabilityManager.update(name, config);
        return "Updated " + field + " for '" + name + "' to: " + value;
    }

    private void sendReply(final String message) {
        try {
            final ProcessBuilder pb = new ProcessBuilder("osascript", SCRIPT_PATH, adminChat,
                    "[Admin]: " + message);
            final Process process = pb.start();

            final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = error.readLine()) != null) {
                System.err.println("[AdminReply Error] " + line);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
