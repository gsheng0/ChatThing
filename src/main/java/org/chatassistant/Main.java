package org.chatassistant;

import org.chatassistant.ai.agent.AgenticGeminiAgent;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.entities.Message;
import org.chatassistant.thread.PollingTask;
import org.chatassistant.thread.ProcessingTask;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

//Negative amounts in spreadsheet means that person should send that money
//Positive amounts means that person should receive that money
public class Main {
    public static final String TEST_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/testPrompt";
    public static final String TEST_PROMPT_PATH2 = "/Users/georgesheng/proj/scheduler2/src/main/resources/testPrompt2";
    public static final String NORMAL_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt";
    public static final String NORMAL_PROMPT_PATH2 = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt2";
    public static final String NORMAL_PROMPT_PATH3 = "/Users/georgesheng/proj/scheduler2/src/main/resources/expenseTrackingPrompt3";
    public static final String EXTRACT_RECEIPT_PROMPT_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/extractReceiptPrompt";

    public static final String PROMPT = NORMAL_PROMPT_PATH3;

    public static void main(String[] args) {
        final AiAgent agent = AgenticGeminiAgent.getInstance();
        final BlockingDeque<List<Message>> messageDeque = new LinkedBlockingDeque<>();
        final PollingTask poller = new PollingTask(messageDeque);
        final ProcessingTask processor = new ProcessingTask(messageDeque, agent);
        final Thread pollingThread = new Thread(poller, "PollingThread");
        final Thread processingThread = new Thread(processor, "ProcessingThread");

        pollingThread.start();
        processingThread.start();

        System.out.println(AiAgent.getAllTools().size() + " tools");
        System.out.println("Main: Threads started. Application running.");

        // In a real application, you might have a way to gracefully shut down these threads,
        // e.g., by setting a volatile flag that their run methods check.
        // For this skeleton, they will run indefinitely until the JVM exits.
    }
}
