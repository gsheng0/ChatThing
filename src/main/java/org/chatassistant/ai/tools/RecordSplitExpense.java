package org.chatassistant.ai.tools;

import org.chatassistant.google.GoogleSheets;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.chatassistant.data.Contact;

import java.util.Arrays;
import java.util.List;

@AiAgentTool
public class RecordSplitExpense {
    private final GoogleSheets sheets;
    private final Contact contact;

    public RecordSplitExpense(final GoogleSheets sheets, final Contact contact) {
        this.sheets = sheets;
        this.contact = contact;
    }

    /**
     * @param amount the amount to add to ledger, split between the names specified
     * @param names the names to add the split expense to
     * @return errors, if any
     */
    @ToolMethod
    public String recordSplitExpense(final double amount, final String names) {
        final List<String> validNames = Arrays.stream(names.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(contact.getNameToColMap()::containsKey)
                .toList();
        final double splitAmount = amount / (1.0 * validNames.size());
        for (final String name : validNames) {
            final String cell = contact.getNameToColMap().get(name) + "2";
            try {
                final double currentValue = Double.parseDouble(sheets.getCell(sheets.expenseSheet, cell));
                sheets.updateExpenseCell(cell, Double.toString(currentValue + splitAmount));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return "Error parsing amount. Try again";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error updating cell. Try again";
            }
        }
        return "";
    }
}
