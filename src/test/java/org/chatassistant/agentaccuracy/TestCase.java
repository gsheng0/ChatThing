package org.chatassistant.agentaccuracy;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.chatassistant.GoogleSheets;
import org.chatassistant.Logger;
import org.chatassistant.ai.agent.AgenticGeminiAgent;
import org.chatassistant.ai.agent.AiAgent;
import org.chatassistant.config.AiAgentConfig;
import org.chatassistant.data.Receipt;
import org.checkerframework.checker.units.qual.A;

import java.util.Arrays;
import java.util.List;

@SuperBuilder(toBuilder = true)
public class TestCase {
    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private Receipt receipt;

    @Setter
    @Getter
    private List<String> messages;

    @Setter
    @Getter
    private double[] expectedValues;

    private static final Logger logger = Logger.of(TestCase.class);

    public void run(){
        final GoogleSheets sheets = GoogleSheets.getInstance();
        AgenticGeminiAgent agent = new AgenticGeminiAgent(new AiAgentConfig());
        logger.log("Testing case {} with prompt {}", name, receipt.getName());
        for(final String message : messages){
            logger.log("Actor asked: {}", message);
            logger.log("Agent responded: {}", agent.ask(message));
        }

        final String[] actualState = sheets.getCellRange(GoogleSheets.EXPENSE_SHEET, "A2:G2")[0];
        boolean equal = true;
        for(int i = 0; i < actualState.length; i++){
            final double actualValue = Double.parseDouble(actualState[i]);
            if(actualValue != expectedValues[i]){
                equal = false;
                break;
            }
        }

        if(equal){
            logger.log("Test Passed!");
        } else{
            logger.log("Test Failed! Expected: {}, Found: {}", Arrays.toString(expectedValues), Arrays.toString(actualState));
        }
    }
}
