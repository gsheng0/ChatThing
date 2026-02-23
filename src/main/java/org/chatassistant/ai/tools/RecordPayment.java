package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.data.Contact;

@AiAgentTool
public class RecordPayment {
    private final GoogleSheets sheets;
    private final Contact contact;
    private static final Logger LOGGER = Logger.of(RecordPayment.class);

    public RecordPayment(final GoogleSheets sheets, final Contact contact) {
        this.sheets = sheets;
        this.contact = contact;
    }

    /**
     * Records a payment on the spreadsheet
     * @param sender the name of the person paying the money
     * @param receiver the name of the person receiving the money
     * @param amount the amount paid by the sender to the receiver
     * @return errors, if any
     */
    public String recordPayment(String sender, String receiver, final double amount) {
        sender = sender.toLowerCase();
        receiver = receiver.toLowerCase();

        if (!contact.getNameToColMap().containsKey(sender)) {
            return "Sender not recognized. Try again";
        }

        if (!contact.getNameToColMap().containsKey(receiver)) {
            return "Receiver not recognized. Try again";
        }

        try {
            final String senderCell = contact.getNameToColMap().get(sender) + "2";
            final String receiverCell = contact.getNameToColMap().get(receiver) + "2";
            final double senderCellAmount = Double.parseDouble(sheets.getCell(sheets.expenseSheet, senderCell));
            final double receiverCellAmount = Double.parseDouble(sheets.getCell(sheets.expenseSheet, receiverCell));
            sheets.updateExpenseCell(senderCell, Double.toString(senderCellAmount + amount));
            sheets.updateExpenseCell(receiverCell, Double.toString(receiverCellAmount - amount));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "There is an error with the group chat. Use the sendTextMessage tool to notify the group chat about this error";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating spreadsheet. Try again.";
        }
        return "";
    }
}
