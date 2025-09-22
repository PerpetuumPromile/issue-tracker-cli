package com.perpetuum.issue_tracker.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class GoogleSheetsFacade {

    private static final String APPLICATION_NAME = "Issue Tracker CLI";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final Sheets service;
    private final String spreadsheetId;
    private final String defaultSheetName;

    public GoogleSheetsFacade(String spreadsheetId) throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        this.service = getSheetsService();
        this.defaultSheetName = getFirstSheetName();  // 🟢 dynamicky názov hárku
    }

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) {
            throw new RuntimeException("⚠️ Missing credentials.json in resources!");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    // 🟢 Zistí názov prvého hárku v dokumente
    private String getFirstSheetName() throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        if (sheets == null || sheets.isEmpty()) {
            throw new RuntimeException("❌ Spreadsheet has no sheets!");
        }
        return sheets.get(0).getProperties().getTitle();
    }

    // 🟢 Inicializácia hlavičky, ak je prázdny sheet
    public void initializeHeaderIfEmpty() throws IOException {
        String range = defaultSheetName + "!A1:F1";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            List<Object> header = Arrays.asList("ID", "Description", "Parent ID", "Status", "Created at", "Updated at");
            ValueRange body = new ValueRange().setValues(List.of(header));

            service.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("✅ Header initialized in Google Sheet.");
        } else {
            System.out.println("ℹ️ Header already exists, skipping initialization.");
        }
    }

    // 🟢 Append nového riadku
    public void appendRow(List<Object> row) throws IOException {
        String range = defaultSheetName + "!A:F"; // zapisujeme do celého rozsahu
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));
        service.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    // 🟢 Čítanie všetkých riadkov
    public List<List<Object>> readAll() throws IOException {
        String range = defaultSheetName + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    public void updateRow(int rowIndex, List<Object> row) throws IOException {
    String range = defaultSheetName + "!A" + rowIndex + ":F" + rowIndex;
    ValueRange body = new ValueRange().setValues(Collections.singletonList(row));
    service.spreadsheets().values()
            .update(spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute();
}
}
