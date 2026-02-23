package org.chatassistant.ai.tools;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@AiAgentTool
public class SendTextMessage {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final Logger LOGGER = Logger.of(SendTextMessage.class);

    /**
     * Sends a text message to an iMessage group chat.
     * @param chatName the exact name of the group chat to send to
     * @param message the content of the text message to be sent
     */
    public void sendTextMessage(final String chatName, final String message) {
        final String paddedMessage = "[Intern]: " + message;
        final Process process;
        try {
            process = new ProcessBuilder("osascript", SCRIPT_PATH, chatName, paddedMessage).start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Output] " + line);
            }
            while ((line = error.readLine()) != null) {
                System.err.println("[Error] " + line);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            process.destroy();
        }
    }
}
