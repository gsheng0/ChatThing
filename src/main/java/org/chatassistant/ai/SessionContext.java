package org.chatassistant.ai;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SessionContext {
    final List<ChatEntry> history;

    public SessionContext() {
        this.history = new ArrayList<>();
    }

    public void appendEntry(final ChatEntry entry) {
        this.history.add(entry);
    }
}
