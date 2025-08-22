package org.chatassistant.ai.tools;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@AiAgentTool
public class SendTextMessage {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final String GROUP_CHAT_NAME = "Sewerslide Pack";
    private static final Logger LOGGER = Logger.of(SendTextMessage.class);
    /**
     * Sends a text message to the group chat with my friends
     * @param message the content of the text message to be sent
     */
    public static void sendTextMessage(final String message){
        final String paddedMessage = "[Intern]: " + message;
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript", SCRIPT_PATH, GROUP_CHAT_NAME, paddedMessage);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Output] " + line);
            }

            while ((line = error.readLine()) != null) {
                System.err.println("[Error] " + line);
            }

            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
