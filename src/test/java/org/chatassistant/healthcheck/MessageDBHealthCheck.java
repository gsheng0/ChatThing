package org.chatassistant.healthcheck;

import org.chatassistant.data.MessageDB;

public class MessageDBHealthCheck implements HealthCheck {
    public static void main(String[] args){
        final MessageDB messageDB = MessageDB.getInstance();
        messageDB.getRecentMessages();
    }
}
