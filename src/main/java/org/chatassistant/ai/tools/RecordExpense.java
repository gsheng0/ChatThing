package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.Logger;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.data.Contact;

@AiAgentTool
public class RecordExpense {
    private final GoogleSheets sheets;
    private final Contact contact;
    private static final Logger LOGGER = Logger.of(RecordExpense.class);

    public RecordExpense(final GoogleSheets sheets, final Contact contact) {
        this.sheets = sheets;
        this.contact = contact;
    }

    /**
     *
     * @param name the name to add the expense to
     * @param amount the amount to add to ledger. positive amount if that person paid for it.
     *               negative amount if that person owes that money
     * @return errors, if any
     */
    public String recordExpense(String name, final double amount) {
        name = name.toLowerCase();

        if (!contact.getNameToColMap().containsKey(name)) {
            return "Name not recognized. Try again";
        }

        try {
            final String cell = contact.getNameToColMap().get(name) + "2";
            final double currentValue = Double.parseDouble(sheets.getCell(sheets.expenseSheet, cell));
            sheets.updateExpenseCell(cell, Double.toString(currentValue + amount));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Error parsing amount. Try again";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating cell. Try again";
        }
        return "";
    }
}
