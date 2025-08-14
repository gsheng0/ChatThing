package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.data.Contact;

public class RecordPayment implements AiAgentTool {
    private static final GoogleSheets SHEETS = GoogleSheets.getInstance();
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

        if(!CONTACT.NAME_TO_COL_MAP.containsKey(sender)){
            return "Sender not recognized. Try again";
        }

        if(!CONTACT.NAME_TO_COL_MAP.containsKey(receiver)){
            return "Receiver not recognized. Try again";
        }

        try {
            final String senderCell = CONTACT.NAME_TO_COL_MAP.get(sender) + "2";
            final String receiverCell = CONTACT.NAME_TO_COL_MAP.get(receiver) + "2";
            final double senderCellAmount = Double.parseDouble(SHEETS.getCell(GoogleSheets.CONTACT_SHEET, senderCell));
            final double receiverCellAmount = Double.parseDouble(SHEETS.getCell(GoogleSheets.CONTACT_SHEET, receiverCell));
            SHEETS.updateExpenseCell(senderCell, Double.toString(senderCellAmount + amount));
            SHEETS.updateExpenseCell(receiverCell, Double.toString(receiverCellAmount - amount));
        } catch(NumberFormatException e){
            e.printStackTrace();
            return "There is an error with the group chat. Use the sendTextMessage tool to notify the group chat about this error";
        } catch(Exception e){
            e.printStackTrace();
            return "Error updating spreadsheet. Try again.";
        }
        return "";
    }
}
