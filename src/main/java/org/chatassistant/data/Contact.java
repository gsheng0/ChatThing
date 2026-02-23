package org.chatassistant.data;

import lombok.Getter;
import org.chatassistant.GoogleSheets;
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
        final String[] names = sheets.getCellRange(sheets.expenseSheet, "A1:G1")[0];
        final HashMap<String, String> nameToColMapBuilder = new HashMap<>();
        char col = 'A';
        for (final String name : names) {
            nameToColMapBuilder.put(name.toLowerCase(), String.valueOf(col++));
        }
        nameToColMap = Collections.unmodifiableMap(nameToColMapBuilder);

        // Sender id to name map
        final String[][] senderIdToName = sheets.getCellRange(sheets.contactSheet, "A1:B13");
        final HashMap<String, String> idToNameMapBuilder = new HashMap<>();
        for (final String[] pair : senderIdToName) {
            idToNameMapBuilder.put(pair[0], pair[1]);
        }
        idToNameMap = Collections.unmodifiableMap(idToNameMapBuilder);
    }
}
