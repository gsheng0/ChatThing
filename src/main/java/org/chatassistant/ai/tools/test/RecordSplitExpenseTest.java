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
    private final Contact contact;

    public RecordSplitExpenseTest(final Contact contact) {
        this.contact = contact;
    }

    /**
     * @param amount the amount to add to ledger, split between the names specified
     * @param names the names to add the split expense to
     * @return errors, if any
     */
    public String recordSplitExpenseTest(final double amount, final String names) {
        final List<String> validNames = Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(contact.getNameToColMap()::containsKey)
                .map(String::toLowerCase)
                .toList();
        final double splitAmount = amount / (validNames.size());
        final Map<String, Double> map = TestContextHolder.ledger;
        for (final String name : validNames) {
            map.put(name, map.getOrDefault(name, 0.0) + splitAmount);
        }
        return "";
    }
}
