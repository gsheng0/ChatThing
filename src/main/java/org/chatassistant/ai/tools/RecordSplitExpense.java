package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.data.Contact;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AiAgentTool
public class RecordSplitExpense {
    private static final Map<String, String> NAME_MAP = Contact.getInstance().ID_TO_NAME_MAP;
    private static final GoogleSheets SHEETS = GoogleSheets.getInstance();
    /**
     * @param amount the amount to add to ledger, split between the names specified
     * @param names the names to add the split expense to
     * @return errors, if any
     */
    public static String recordSplitExpense(final double amount, final String[] names){
        final List<String> validNames = Arrays.stream(names)
                .filter(NAME_MAP::containsKey)
                .map(String::toLowerCase)
                .toList();
        final double splitAmount = amount/(1.0 * validNames.size());
        for(final String name : validNames){
            final String cell = NAME_MAP.get(name) + "2";
            try{
                final double currentValue = Double.parseDouble(SHEETS.getCell(GoogleSheets.EXPENSE_SHEET, cell));
                SHEETS.updateExpenseCell(cell, Double.toString(currentValue + splitAmount));
            } catch(NumberFormatException e){
                e.printStackTrace();
                return "Error parsing amount. Try again";
            } catch(Exception e){
                e.printStackTrace();
                return "Error updating cell. Try again";
            }
        }
        return "";
    }
}
