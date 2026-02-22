package org.chatassistant.agentaccuracy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.chatassistant.GoogleSheets;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.ai.tools.test.TestContextHolder;
import org.chatassistant.data.Receipt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
public class TestCase {
    private String name;
    private Receipt receipt;
    private List<String> messages;
    private Map<String, Double> expectedValues;
    private AiAgent<Void> aiAgent;

    private static final Logger logger = Logger.of(TestCase.class);

    public void run(){
        logger.log("Testing case {} with prompt {}", name, receipt.getName());
        aiAgent.ask(null, receipt.getReceipt());
        for(final String message : messages){
            logger.log("Actor asked: {}", message);
            logger.log("Agent responded: {}", aiAgent.ask(null, message));
        }

        final Map<String, Double> actualValues = TestContextHolder.ledger;
        boolean equal = actualValues.size() == expectedValues.size();
        if(equal){
            for(final String name : expectedValues.keySet()){
                if(!actualValues.containsKey(name) || !actualValues.get(name.toLowerCase()).equals(expectedValues.get(name))){
                    equal = false;
                    break;
                }
            }
        }


        if(equal){
            logger.log("Test Passed!");
        } else{
            logger.log("Test Failed! Expected: {}, Found: {}", expectedValues, actualValues);
        }
    }
}
