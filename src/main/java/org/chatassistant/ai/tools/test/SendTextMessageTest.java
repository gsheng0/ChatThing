package org.chatassistant.ai.tools.test;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTestTool;

@AiAgentTestTool
public class SendTextMessageTest {
    private static final Logger LOGGER = Logger.of(SendTextMessageTest.class);
    /**
     * Sends a text message to the group chat with my friends
     * @param message the content of the text message to be sent
     */
    public static void sendTextMessage(final String message){
        LOGGER.log("Agent sent message: \"{}\"", message);
    }

}
