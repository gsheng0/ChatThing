package org.chatassistant.agentaccuracy;

import org.chatassistant.ai.agent.ClaudeAgent;
import org.chatassistant.ai.agent.AgentContext;
import org.chatassistant.ai.tools.ToolRegistry;
import org.chatassistant.ai.tools.test.TestContextHolder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Measures how often the expenses agent calls sendTextMessage with a wrong chat name.
 *
 * The real tool is sendTextMessage(String chatName, String message). Every message the
 * agent receives is prefixed with "[Chat: Sewerslide Pack]" (as ChatProcessingTask does),
 * so the agent should always pass "Sewerslide Pack" as chatName. Any other value is a
 * hallucination.
 *
 * Run manually — requires ANTHROPIC_API_KEY in the environment.
 */
@Disabled("Integration test — requires ANTHROPIC_API_KEY. Run manually.")
@SpringBootTest
public class ChatNameHallucinationTest {

    private static final String EXPECTED_CHAT_NAME = "Sewerslide Pack";
    private static final String PROMPT_PATH = "src/main/resources/expenseTrackingPrompt2";
    private static final String MODEL = "claude-sonnet-4-6";

    @Autowired
    private ToolRegistry toolRegistry;

    private static final List<String> TEST_MESSAGES = List.of(
        // typical expense messages
        "[Chat: Sewerslide Pack]\nGeorge: I had the pizza, $18",
        "[Chat: Sewerslide Pack]\nAlice: Can you split the $40 dinner between me and Bob?",
        "[Chat: Sewerslide Pack]\nShubh: I paid for everyone, total was $120",
        "[Chat: Sewerslide Pack]\nBob: What's the current balance?",
        "[Chat: Sewerslide Pack]\nAlice: Bob paid me back $20",
        "[Chat: Sewerslide Pack]\nGeorge: I got the drinks, $35 total",
        "[Chat: Sewerslide Pack]\nShubh: Record $15 for coffee, just me",
        "[Chat: Sewerslide Pack]\nBob: Alice and I split the $50 Uber",
        // messages agent should ignore
        "[Chat: Sewerslide Pack]\nGeorge: haha that's so funny",
        "[Chat: Sewerslide Pack]\nAlice: good morning everyone",
        "[Chat: Sewerslide Pack]\nBob: anyone want to grab food later?",
        // edge cases
        "[Chat: Sewerslide Pack]\nGeorge: settle up",
        "[Chat: Sewerslide Pack]\nAlice: what do I owe?",
        "[Chat: Sewerslide Pack]\nShubh: I had the fettuccine and the pineapple juice, $26 total"
    );

    @Test
    void measureChatNameHallucination() {
        final ClaudeAgent agent = new ClaudeAgent(MODEL, PROMPT_PATH, toolRegistry);
        final AgentContext context = new AgentContext();

        TestContextHolder.reset();

        int totalCalls = 0;
        int wrongChatName = 0;

        for (final String message : TEST_MESSAGES) {
            TestContextHolder.sendTextMessageCalls.clear();

            agent.ask(context, message, List.of());

            for (final String[] call : TestContextHolder.sendTextMessageCalls) {
                totalCalls++;
                final String chatName = call[0];
                if (!EXPECTED_CHAT_NAME.equals(chatName)) {
                    wrongChatName++;
                    System.out.printf("[HALLUCINATION] Message: \"%s\"%n  Expected chatName: \"%s\"%n  Got: \"%s\"%n%n",
                            message.substring(message.indexOf('\n') + 1), EXPECTED_CHAT_NAME, chatName);
                }
            }
        }

        System.out.printf("%n=== Chat Name Hallucination Report ===%n");
        System.out.printf("Messages tested : %d%n", TEST_MESSAGES.size());
        System.out.printf("sendTextMessage calls : %d%n", totalCalls);
        System.out.printf("Incorrect chatName    : %d%n", wrongChatName);
        if (totalCalls > 0) {
            System.out.printf("Hallucination rate    : %.1f%%%n", 100.0 * wrongChatName / totalCalls);
        } else {
            System.out.println("Hallucination rate    : N/A (agent made no sendTextMessage calls)");
        }
    }
}
