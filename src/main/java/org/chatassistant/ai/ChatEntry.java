package org.chatassistant.ai;

import lombok.Getter;

@Getter
public class ChatEntry {
    public enum Role {
        USER("user"),
        SYSTEM("system"),
        ADMIN("admin");

        private String name;

        Role(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    private String role;
    private String content;

    public ChatEntry(final String role, final String content) {
        this.role = role;
        this.content = content;
    }

    public ChatEntry(final Role role, final String content) {
        this.role = role.getName();
        this.content = content;
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.role);
        builder.append(": ");
        builder.append(this.content);
        return builder.toString();
    }
}
