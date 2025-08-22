package org.chatassistant.ai.tools;

import org.chatassistant.GoogleSheets;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.data.Contact;

import java.util.HashMap;
import java.util.Map;

@AiAgentTool
public class GetSummary {
    private static final GoogleSheets SHEETS = GoogleSheets.getInstance();
    private static final Contact CONTACT = Contact.getInstance();

    /**
     * Retrieves summary of current state of expenses
     *
     * @return a map of names to amount owed to that person
     */
    public static Map<String, Double> getSummary() {
        final Map<String, Double> summary = new HashMap<>();
        final String[] nums = SHEETS.getCellRange(GoogleSheets.EXPENSE_SHEET, "A2:G2")[0];
        for (final String name : CONTACT.NAME_TO_COL_MAP.keySet()) {
            summary.put(name, Double.parseDouble(nums[CONTACT.NAME_TO_COL_MAP.get(name).charAt(0) - 'A']));
        }
        return summary;
    }

}