package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.data.Contact;

public class RecordExpense implements AiAgentTool {
    private static final GoogleSheets SHEETS = GoogleSheets.getInstance();
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

        if(!CONTACT.NAME_TO_COL_MAP.containsKey(name)){
            return "Name not recognized. Try again";
        }

        try{
            final String cell = CONTACT.NAME_TO_COL_MAP.get(name) + "2";
            final double currentValue = Double.parseDouble(SHEETS.getCell(GoogleSheets.EXPENSE_SHEET, cell));
            SHEETS.updateExpenseCell(cell, Double.toString(currentValue + amount));
        } catch(NumberFormatException e){
            e.printStackTrace();
            return "Error parsing amount. Try again";
        } catch(Exception e){
            e.printStackTrace();
            return "Error updating cell. Try again";
        }
        return "";
    }
}
