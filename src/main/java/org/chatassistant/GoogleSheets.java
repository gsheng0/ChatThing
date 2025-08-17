package org.chatassistant;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.util.List;

public class GoogleSheets {
    private static GoogleSheets instance;
    public static GoogleSheets getInstance() {
        if (instance == null) {
            instance = new GoogleSheets();
        }
        return instance;
    }

    private static final String CREDENTIALS_PATH = "/Users/georgesheng/proj/scheduler2/src/main/resources/credentials.json";
    private static final String SPREADSHEET_ID = "1cn_ziMHSCvc_Xn8RWuN7yZg40VIoxBIOvGwKtzhtcJM";
    public static final String EXPENSE_SHEET = "Sheet1";
    public static final String CONTACT_SHEET = "ContactMap";

    private final Sheets sheets;
    private final JsonFactory jsonFactory;
    private GoogleSheets() {
        try{
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = GsonFactory.getDefaultInstance();
            sheets = new Sheets.Builder(httpTransport, jsonFactory, getCredentials(httpTransport))
                    .setApplicationName("Google Sheets API Quickstart")
                    .build();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void updateExpenseCell(final String cellAddress, final String content) {
        try {
            final String range = EXPENSE_SHEET + "!" + cellAddress;

            List<List<Object>> values = List.of(List.of(content));
            ValueRange body = new ValueRange().setValues(values);

            sheets.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (Exception e) {
            throw new RuntimeException("Error updating cell " + cellAddress + ": " + e.getMessage(), e);
        }
    }

    public String[][] getCellRange(final String sheetName, final String range) {
        try {
            final ValueRange valueRange = sheets.spreadsheets().values().get(SPREADSHEET_ID, sheetName+ "!" + range).execute();
            final List<List<Object>> values = valueRange.getValues();

            if (values == null || values.isEmpty()) {
                return new String[0][0]; // Return an empty 2D array if no data is found
            }

            final int numRows = values.size();
            int numCols = 0;
            for(List<Object> row : values) {
                numCols = Math.max(numCols, row.size());
            }

            final String[][] cells = new String[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                final List<Object> row = values.get(i);
                for (int j = 0; j < row.size(); j++) {
                    cells[i][j] = row.get(j).toString();
                }
            }
            return cells;
        } catch (final Exception e) {
            throw new RuntimeException("Error retrieving cell range: " + e.getMessage(), e);
        }
    }

    public String getCell(final String sheetName, final String cell){
        return getCellRange(sheetName, cell)[0][0];
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        final FileInputStream in = new FileInputStream(CREDENTIALS_PATH);
        final GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, jsonFactory, clientSecrets, List.of(SheetsScopes.SPREADSHEETS))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
