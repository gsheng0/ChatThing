package org.chatassistant.ai.tools.test;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.chatassistant.data.Contact;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AiAgentTestTool
public class RecordSplitExpenseTest {
    private static final Logger LOGGER = Logger.of(RecordSplitExpenseTest.class);
    private static final Contact CONTACT = Contact.getInstance();

    /**
     * @param amount the amount to add to ledger, split between the names specified
     * @param names the names to add the split expense to
     * @return errors, if any
     */
    public static String recordSplitExpenseTest(final double amount, final String[] names){
        final List<String> validNames = Arrays.stream(names)
                .filter(CONTACT.NAME_TO_COL_MAP::containsKey)
                .map(String::toLowerCase)
                .toList();
        final double splitAmount = amount/(validNames.size());
        final Map<String, Double> map = TestContextHolder.ledger;
        for(final String name : validNames){
            map.put(name, map.getOrDefault(name, 0.0) + splitAmount);
        }
        return "";
    }
}
