package org.chatassistant.garbage;

import org.chatassistant.ChatSession;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.GeminiAgent;
import org.chatassistant.config.LoggingConfigurationProperties;
import org.chatassistant.data.MessageDB;
import org.chatassistant.entities.Message;

import jakarta.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

public class AssistantService {
    private static final int PASSIVE_SLEEP_MS = 5000;
    private static final int ACTIVE_SLEEP_MS  = 1000;
    private static final int ACTIVE_COUNTDOWN = 5;
    private static final String LOG_BASE = "/Users/georgesheng/proj/scheduler2/logs/";

    private static final Logger logger = Logger.of(AssistantService.class);

    private final GeminiAgent agent;
    private final Map<String, ChatSession> sessions = new LinkedHashMap<>();
    private final Set<Message> seenMessages = new HashSet<>();

    private int status = 0; // 0 = passive, 1 = active
    private int activeCounter = 0;

    public AssistantService(final GeminiAgent agent, final LoggingConfigurationProperties loggingConfig) {
        this.agent = agent;
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Logger.init(LOG_BASE + loggingConfig.getOutputFolder() + "/" + timestamp + ".log");
        seenMessages.addAll(MessageDB.getInstance().getRecentMessages());
    }

    @PostConstruct
    public void start() {
        new Thread(this::run, "AssistantLoop").start();
    }

    private void run() {
        logger.log("Assistant started.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final boolean hadNewMessages = poll();
                if (hadNewMessages) {
                    status = 1;
                    activeCounter = ACTIVE_COUNTDOWN;
                } else if (--activeCounter <= 0) {
                    status = 0;
                }
                Thread.sleep(status == 1 ? ACTIVE_SLEEP_MS : PASSIVE_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean poll() {
        final List<Message> recent = MessageDB.getInstance().getRecentMessages();
        final Map<String, List<Message>> newByChatName = new LinkedHashMap<>();

        for (final Message message : recent) {
            if (seenMessages.contains(message) || message.getText().startsWith("[Intern]:")) continue;
            seenMessages.add(message);
            newByChatName.computeIfAbsent(message.getChatName(), k -> new ArrayList<>()).add(message);
        }

        for (final Map.Entry<String, List<Message>> entry : newByChatName.entrySet()) {
            final ChatSession session = sessions.computeIfAbsent(entry.getKey(), ChatSession::new);
            process(session, entry.getValue());
        }

        return !newByChatName.isEmpty();
    }

    private void process(final ChatSession session, final List<Message> messages) {
        final StringBuilder prompt = new StringBuilder();
        final List<String> imagePaths = new ArrayList<>();

        for (final Message message : messages) {
            final String imagePath = message.getImagePath();
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("None")) {
                imagePaths.add(imagePath);
            }
            if (message.getText() != null && !message.getText().isEmpty()) {
                prompt.append(message.getSender()).append(": ").append(message.getText()).append("\n");
            }
        }

        if (!prompt.isEmpty() || !imagePaths.isEmpty()) {
            logger.log("[{}] Processing: {}", session.getChatName(), prompt.toString().trim());
            final String response = agent.ask(session.getContext(), prompt.toString(), imagePaths);
            logger.log("[{}] Response: {}", session.getChatName(), response);
        }
    }
}
