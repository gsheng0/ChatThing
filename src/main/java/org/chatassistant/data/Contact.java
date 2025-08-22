package org.chatassistant.data;

import org.chatassistant.GoogleSheets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Contact {
    private static Contact instance;
    public static Contact getInstance() {
        if (instance == null) {
            instance = new Contact();
        }
        return instance;
    }

    public final Map<String, String> NAME_TO_COL_MAP;
    public final Map<String, String> ID_TO_NAME_MAP;
    private Contact(){
        final GoogleSheets sheets = GoogleSheets.getInstance();

        //Name to col map
        final String[] names = sheets.getCellRange(GoogleSheets.EXPENSE_SHEET, "A1:G1")[0];
        final HashMap<String, String> nameToColMap = new HashMap<>();
        char col = 'A';
        for(final String name : names){
            nameToColMap.put(name.toLowerCase(), String.valueOf(col++));
        }
        NAME_TO_COL_MAP = Collections.unmodifiableMap(nameToColMap);

        //Sender id to name map
        final String[][] senderIdToName = sheets.getCellRange(GoogleSheets.CONTACT_SHEET, "A1:B13");
        final HashMap<String, String> senderIdToNameMap = new HashMap<>();
        for(final String[] pair : senderIdToName){
            senderIdToNameMap.put(pair[0], pair[1]);
        }
        ID_TO_NAME_MAP = Collections.unmodifiableMap(senderIdToNameMap);
    }
}
