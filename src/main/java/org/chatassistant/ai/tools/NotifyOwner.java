package org.chatassistant.ai.tools;

import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@AiAgentTool
public class NotifyOwner {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final String OWNER_ID = "owner@icloud.com"; // hardcoded owner Apple ID

    /**
     * Sends a direct iMessage to the owner of this system.
     * Use this to report errors, flag anomalies, or send alerts that only the owner should see.
     * @param message the message content
     */
    @ToolMethod
    public void notifyOwner(final String message) {
        final String paddedMessage = "[Intern]: " + message;
        final Process process;
        try {
            process = new ProcessBuilder("osascript", SCRIPT_PATH, OWNER_ID, paddedMessage).start();
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
