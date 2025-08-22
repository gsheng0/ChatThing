package org.chatassistant.ai.tools.test;

import org.chatassistant.Logger;
import org.chatassistant.ai.tools.RecordPayment;
import org.chatassistant.ai.tools.annotation.AiAgentTestTool;
import org.chatassistant.data.Contact;

import java.util.Map;

@AiAgentTestTool
public class RecordPaymentTest {
    private static final Logger LOGGER = Logger.of(RecordPayment.class);
    private static final Contact CONTACT = Contact.getInstance();

    /**
     * Records a payment on the spreadsheet
     * @param sender the name of the person paying the money
     * @param receiver the name of the person receiving the money
     * @param amount the amount paid by the sender to the receiver
     * @return errors, if any
     */
    public static String recordPayment(String sender, String receiver, final double amount){
        sender = sender.toLowerCase();
        receiver = receiver.toLowerCase();
        LOGGER.log("Recording payment of {} from {} to {}", amount, sender, receiver);
        if(!CONTACT.NAME_TO_COL_MAP.containsKey(sender)){
            LOGGER.log("Name {} was not recognized", sender);
            return "Sender not recognized. Try again";
        }

        if(!CONTACT.NAME_TO_COL_MAP.containsKey(receiver)){
            LOGGER.log("Name {} was not recognized", receiver);
            return "Receiver not recognized. Try again";
        }

        final Map<String, Double> map = TestContextHolder.ledger;
        map.put(sender, map.getOrDefault(sender, 0.0) - amount);
        map.put(receiver, map.getOrDefault(receiver, 0.0) + amount);
        return "";
    }
}
