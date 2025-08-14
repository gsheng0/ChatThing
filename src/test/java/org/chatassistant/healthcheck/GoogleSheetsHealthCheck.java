package org.chatassistant.healthcheck;

import org.chatassistant.GoogleSheets;

public class GoogleSheetsHealthCheck implements HealthCheck {
    public static void main(String[] args){
        final GoogleSheets sheets = GoogleSheets.getInstance();
        sheets.getCell(GoogleSheets.CONTACT_SHEET, "A1:G2");
    }

}
