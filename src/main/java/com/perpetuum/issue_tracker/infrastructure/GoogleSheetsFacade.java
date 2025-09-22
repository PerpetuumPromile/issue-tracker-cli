package com.perpetuum.issue_tracker.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * GoogleSheetsFacade
 * ------------------
 * A facade over the Google Sheets API.
 *
 * Responsibilities:
 * - Authenticate via `credentials.json`
 * - Perform read/write operations on the spreadsheet
 *
 * Improvements:
 * - Uses logging instead of System.out
 * - Throws custom exceptions instead of generic RuntimeExceptions
 * - Accepts `Sheets` client through constructor (dependency injection),
 *   which improves testability and respects DIP.
 */
public class GoogleSheetsFacade {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsFacade.class);
    private static final String APPLICATION_NAME = "Issue Tracker CLI";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final Sheets service;          // Now injected → better for testing
    private final String spreadsheetId;
    private final String defaultSheetName;

    /**
     * Constructor with dependency injection (preferred).
     */
    public GoogleSheetsFacade(Sheets service, String spreadsheetId) throws IOException {
        this.spreadsheetId = spreadsheetId;
        this.service = service;
        this.defaultSheetName = getFirstSheetName();
    }

    /**
     * Factory method for creating a real Google Sheets client.
     */
    public static Sheets createSheetsService() throws IOException, GeneralSecurityException {
        InputStream in = GoogleSheetsFacade.class.getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) {
            throw new MissingCredentialsException("Missing credentials.json in resources!");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Get the name of the first sheet.
     */
    private String getFirstSheetName() throws IOException {
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();

        if (sheets == null || sheets.isEmpty()) {
            throw new EmptySpreadsheetException("Spreadsheet has no sheets!");
        }
        return sheets.get(0).getProperties().getTitle();
    }

    /**
     * Initialize header row if missing.
     */
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

            log.info("Header initialized in Google Sheet.");
        } else {
            log.info("Header already exists, skipping initialization.");
        }
    }

    /**
     * Append new row.
     */
    public void appendRow(List<Object> row) throws IOException {
        String range = defaultSheetName + "!A:F";
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));

        service.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        log.debug("Row appended: {}", row);
    }

    /**
     * Read all rows.
     */
    public List<List<Object>> readAll() throws IOException {
        String range = defaultSheetName + "!A:F";
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    /**
     * Update a row by index.
     */
    public void updateRow(int rowIndex, List<Object> row) throws IOException {
        String range = defaultSheetName + "!A" + rowIndex + ":F" + rowIndex;
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));

        service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        log.debug("Row {} updated: {}", rowIndex, row);
    }
}

/**
 * Custom exception when credentials.json is missing.
 */
class MissingCredentialsException extends RuntimeException {
    public MissingCredentialsException(String message) {
        super(message);
    }
}

/**
 * Custom exception when spreadsheet has no sheets.
 */
class EmptySpreadsheetException extends RuntimeException {
    public EmptySpreadsheetException(String message) {
        super(message);
    }
}
