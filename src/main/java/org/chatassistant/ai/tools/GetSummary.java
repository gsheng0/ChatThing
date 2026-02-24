package org.chatassistant.ai.tools;

import org.chatassistant.google.GoogleSheets;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.chatassistant.data.Contact;

import java.util.HashMap;
import java.util.Map;

@AiAgentTool
public class GetSummary {
    private final GoogleSheets sheets;
    private final Contact contact;

    public GetSummary(final GoogleSheets sheets, final Contact contact) {
        this.sheets = sheets;
        this.contact = contact;
    }

    /**
     * Retrieves summary of current state of expenses
     *
     * @return a map of names to amount owed to that person
     */
    @ToolMethod
    public Map<String, Double> getSummary() {
        final Map<String, Double> summary = new HashMap<>();
        final String[] nums = sheets.getCellRange(sheets.expenseSheet, "A2:G2")[0];
        for (final String name : contact.getNameToColMap().keySet()) {
            summary.put(name, Double.parseDouble(nums[contact.getNameToColMap().get(name).charAt(0) - 'A']));
        }
        return summary;
    }
}
