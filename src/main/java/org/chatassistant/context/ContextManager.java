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

    public GeminiContext getOrCreate(final String chat, final String capability) {
        return contexts.computeIfAbsent(key(chat, capability), k ->
                store.load(chat, capability).orElseGet(GeminiContext::new));
    }

    public void afterTurn(final GeminiContext context, final String chat, final String capability) {
        trimmer.trim(context).ifPresent(summary ->
                logger.log("[{}][{}] Context trimmed. Summary: {}", chat, capability,
                        summary.substring(0, Math.min(summary.length(), 200))));
        store.save(chat, capability, context);
    }

    private static String key(final String chat, final String capability) {
        return chat + "|" + capability;
    }
}
