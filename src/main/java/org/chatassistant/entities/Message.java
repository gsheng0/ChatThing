package org.chatassistant.entities;

import lombok.Getter;

@Getter
public class Message {
    private final String timestamp;
    private final String text;
    private final String sender;
    private final String imagePath;
    private final String chatName;

    public Message(String timestamp, String text, String sender, String imagePath) {
        this.timestamp = timestamp;
        this.text = text;
        this.sender = sender;
        this.imagePath = imagePath;
        this.chatName = "[NO CHAT SPECIFIED]";
    }

    public Message(final String timestamp, final String text, final String sender, final String imagePath, final String chatName) {
        this.timestamp = timestamp;
        this.text = text;
        this.sender = sender;
        this.imagePath = imagePath;
        this.chatName = chatName;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Timestamp: ");
        builder.append(timestamp);
        builder.append("\n");
        builder.append("Text: ");
        builder.append(text);
        builder.append("\n");
        builder.append("Sender: ");
        builder.append(sender);
        builder.append("\n");
        builder.append("ImagePath: ");
        builder.append(imagePath);
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(timestamp.hashCode() + text.hashCode() + sender.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message message) {
            return message.timestamp.equals(timestamp) && message.text.equals(text) && message.sender.equals(sender);
        }
        return false;
    }
}
