package org.chatassistant.agentaccuracy;

import org.chatassistant.Logger;
import org.chatassistant.Main;
import org.chatassistant.Util;
import org.chatassistant.data.Receipt;
import org.chatassistant.thread.LoggingTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Test {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final String GROUP_CHAT_NAME = "Sewerslide Pack";

    public static void main(String[] args){
        final BlockingDeque<String> loggingQueue = new LinkedBlockingDeque<String>();
        final LoggingTask loggingTask = new LoggingTask(loggingQueue);
        Logger.setLoggingQueue(loggingQueue);
        final Thread loggingThread = new Thread(loggingTask);
        loggingThread.start();
        final Receipt prompt = Receipt.of("Prompt3", Util.readFile(Main.NORMAL_PROMPT_PATH));
        final TestCase testcase = TestCase.builder()
                .name("Test Case")
                .receipt(prompt)
                .messages(List.of(

                    ))
                .expectedValues(new double[]{})
                .build();

    }

    public static void sendTextMessage(final String message){
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript", SCRIPT_PATH, GROUP_CHAT_NAME, message);
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
