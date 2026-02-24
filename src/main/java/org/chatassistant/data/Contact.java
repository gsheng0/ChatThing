package org.chatassistant.data;

import lombok.Getter;
import org.chatassistant.google.GoogleSheets;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Lazy
@Component
public class Contact {
    @Getter
    private final Map<String, String> nameToColMap;
    @Getter
    private final Map<String, String> idToNameMap;

    public Contact(final GoogleSheets sheets) {
        // Name to col map
        final String[][] nameRows = sheets.getCellRange(sheets.expenseSheet, "A1:G1");
        final HashMap<String, String> nameToColMapBuilder = new HashMap<>();
        if (nameRows.length > 0) {
            char col = 'A';
            for (final String name : nameRows[0]) {
                nameToColMapBuilder.put(name.toLowerCase(), String.valueOf(col++));
            }
        }
        nameToColMap = Collections.unmodifiableMap(nameToColMapBuilder);

        // Sender id to name map
        final String[][] senderIdToName = sheets.getCellRange(sheets.contactSheet, "A1:B13");
        final HashMap<String, String> idToNameMapBuilder = new HashMap<>();
        for (final String[] pair : senderIdToName) {
            if (pair.length >= 2) {
                idToNameMapBuilder.put(pair[0], pair[1]);
            }
        }
        idToNameMap = Collections.unmodifiableMap(idToNameMapBuilder);
    }
}
