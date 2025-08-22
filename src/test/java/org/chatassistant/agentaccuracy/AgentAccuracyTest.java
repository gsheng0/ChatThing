package org.chatassistant.agentaccuracy;

import org.chatassistant.Util;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.data.Receipt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class AgentAccuracyTest {
    private static final String SCRIPT_PATH = "/Users/georgesheng/proj/scheduler2/send_message_to_gc.scpt";
    private static final String GROUP_CHAT_NAME = "Sewerslide Pack";

    private final AiAgent aiAgent;
    private final Receipt lullaReceipt;
    @Autowired
    public AgentAccuracyTest(@Qualifier("chatAssistantAgent") final AiAgent aiAgent) {
        this.aiAgent = aiAgent;
        lullaReceipt = Receipt.of("Receipt Lulla", Util.readFile("src/test/resources/receipt"));
    }

    @Test
    void basicTest(){
        final TestCase testcase = TestCase.builder()
                .aiAgent(aiAgent)
                .name("Basic Test")
                .receipt(lullaReceipt)
                .messages(List.of(
                    "George: I had the coke",
                    "Shubh: I had the fettucine",
                    "Shubh: I also had the pineapple juice"))
                .expectedValues(Map.of(
                        "george", 6.0,
                        "shubh", 35.0))
                .build();
        testcase.run();
    }
}
