package org.chatassistant.context.impl;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import org.chatassistant.Logger;
import org.chatassistant.Util;
import org.chatassistant.ai.agent.AgentContext;
import org.chatassistant.config.ContextConfigurationProperties;
import org.chatassistant.context.ContextTrimmer;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class LlmContextTrimmer implements ContextTrimmer {
    private static final Logger logger = Logger.of(LlmContextTrimmer.class);

    private final Client client;
    private final GenerateContentConfig config;
    private final String modelName;
    private final int verbatimChars;

    public LlmContextTrimmer(final ContextConfigurationProperties contextConfig) {
        this.client = new Client();
        this.modelName = contextConfig.getSummarizationModel();
        this.verbatimChars = contextConfig.getVerbatimChars();
        this.config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(Util.readFile(contextConfig.getSummarizationPromptPath()))))
                .build();
    }

    @PreDestroy
    public void close() {
        client.close();
    }

    @Override
    public Optional<String> trim(final AgentContext context) {
        final List<Content> history = context.getHistory();

        // Count total chars (non-text parts count as 0)
        int totalChars = 0;
        for (final Content content : history) {
            totalChars += textLength(content);
        }

        if (totalChars <= verbatimChars) {
            return Optional.empty();
        }

        // Walk in reverse to find cut point: keep tail that fits within verbatimChars
        int accumulated = 0;
        int cutIndex = history.size(); // exclusive — everything from cutIndex onwards is verbatim
        for (int i = history.size() - 1; i >= 0; i--) {
            final int len = textLength(history.get(i));
            if (accumulated + len > verbatimChars) {
                cutIndex = i + 1;
                break;
            }
            accumulated += len;
            cutIndex = i;
        }

        // The "old" portion to summarize
        final List<Content> oldPortion = history.subList(0, cutIndex);
        final List<Content> verbatimTail = new ArrayList<>(history.subList(cutIndex, history.size()));

        // Build a transcript string from old portion for summarization
        final StringBuilder transcript = new StringBuilder();
        for (final Content content : oldPortion) {
            final String role = content.role().orElse("unknown");
            final String text = content.text();
            if (text != null && !text.isEmpty()) {
                transcript.append(role).append(": ").append(text).append("\n");
            }
        }

        logger.log("Trimming context: summarizing {} turns ({} chars), keeping {} verbatim turns",
                cutIndex, totalChars - accumulated, verbatimTail.size());

        // Single-shot summarization call — no tools, no persistent history
        final String summary;
        try {
            summary = client.models.generateContent(
                    modelName,
                    transcript.toString(),
                    config
            ).text();
        } catch (Exception e) {
            logger.log("Warning: summarization failed, keeping history as-is: {}", e.getMessage());
            return Optional.empty();
        }

        // Replace history in-place
        history.clear();
        history.add(Content.builder()
                .role("user")
                .parts(List.of(Part.fromText("[Summary of earlier conversation: " + summary + "]")))
                .build());
        history.addAll(verbatimTail);

        return Optional.of(summary);
    }

    private static int textLength(final Content content) {
        final String text = content.text();
        return (text != null) ? text.length() : 0;
    }
}
