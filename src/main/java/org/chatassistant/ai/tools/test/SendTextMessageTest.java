package org.chatassistant.ai.tools.test;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTestTool;

@AiAgentTestTool
public class SendTextMessageTest {
    private static final Logger LOGGER = Logger.of(SendTextMessageTest.class);

    /**
     * Sends a text message to an iMessage group chat.
     * @param chatName the exact name of the group chat to send to
     * @param message the content of the text message to be sent
     */
    public void sendTextMessage(final String chatName, final String message) {
        LOGGER.log("Agent sent message to \"{}\": \"{}\"", chatName, message);
        TestContextHolder.sendTextMessageCalls.add(new String[]{chatName, message});
    }
}
