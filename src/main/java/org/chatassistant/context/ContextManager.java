package org.chatassistant.context;

import org.chatassistant.Logger;
import org.chatassistant.ai.agent.GeminiContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContextManager {
    private static final Logger logger = Logger.of(ContextManager.class);

    private final ConcurrentHashMap<String, GeminiContext> contexts = new ConcurrentHashMap<>();
    private final ContextStore store;
    private final ContextTrimmer trimmer;

    public ContextManager(final ContextStore store, final ContextTrimmer trimmer) {
        this.store = store;
        this.trimmer = trimmer;
    }

    public GeminiContext getOrCreate(final String key) {
        return contexts.computeIfAbsent(key, k ->
                store.load(key).orElseGet(GeminiContext::new));
    }

    public void afterTurn(final GeminiContext context, final String key) {
        trimmer.trim(context).ifPresent(summary ->
                logger.log("[{}] Context trimmed. Summary: {}", key,
                        summary.substring(0, Math.min(summary.length(), 200))));
        store.save(key, context);
    }
}
