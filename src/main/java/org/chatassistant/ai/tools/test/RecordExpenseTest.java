package org.chatassistant.ai.tools.test;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.chatassistant.data.Contact;

import java.util.Map;

@AiAgentTestTool
public class RecordExpenseTest {
    private static final Logger LOGGER = Logger.of(RecordExpenseTest.class);
    private static final Contact CONTACT = Contact.getInstance();

    /**
     *
     * @param name the name to add the expense to
     * @param amount the amount to add to ledger. positive amount if that person paid for it.
     *               negative amount if that person owes that money
     * @return errors, if any
     */
    public static String recordExpense(String name, final double amount){
        name = name.toLowerCase();
        LOGGER.log("Recording expense of {} for {}", amount, name);
        if(!CONTACT.NAME_TO_COL_MAP.containsKey(name)){
            LOGGER.log("Name {} was not recognized", name);
            return "Name not recognized. Try again";
        }
        final Map<String, Double> map = TestContextHolder.ledger;
        map.put(name, map.getOrDefault(name, 0.0) + amount);
        return "";
    }
}
