package com.perpetuum.issue_tracker;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.perpetuum.issue_tracker.infrastructure.GoogleSheetsFacade;
import com.perpetuum.issue_tracker.repository.GoogleSheetsIssueRepository;
import com.perpetuum.issue_tracker.service.IssueService;

/**
 * Entry point for the Issue Tracker CLI application.
 * 
 * Responsibilities:
 * - Configure Spring Boot context.
 * - Define Beans for Google Sheets client, Facade, Repository, and Service.
 * - Provide a CLI CommandLineRunner to handle user input.
 */
@SpringBootApplication
public class IssueTrackerApplication {

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    public static void main(String[] args) {
        SpringApplication.run(IssueTrackerApplication.class, args);
    }

    /**
     *  Low-level dependency: Google Sheets API client.
     * Instead of creating it inside the Facade, we configure it here
     * so that higher-level components depend on abstractions.
     */
    @Bean
    public Sheets googleSheetsClient() throws IOException, GeneralSecurityException {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) {
            throw new RuntimeException("Missing credentials.json in resources!");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Issue Tracker CLI").build();
    }

    /**
     * GoogleSheetsFacade bean (injected with Sheets client).
     */
    @Bean
    public GoogleSheetsFacade googleSheetsFacade(Sheets sheetsClient) throws Exception {
        GoogleSheetsFacade facade = new GoogleSheetsFacade(sheetsClient, spreadsheetId);
        facade.initializeHeaderIfEmpty();
        return facade;
    }

    /** Repository bean. */
    @Bean
    public GoogleSheetsIssueRepository issueRepository(GoogleSheetsFacade facade) {
        return new GoogleSheetsIssueRepository(facade);
    }

    /** Service bean. */
    @Bean
    public IssueService issueService(GoogleSheetsIssueRepository repository) {
        return new IssueService(repository);
    }

    /** CLI runner: handles input and delegates to service. */
    @Bean
    public CommandLineRunner commandLineRunner(IssueService issueService) {
        return args -> {
            System.out.println("Issue Tracker CLI running...");

            if (args.length == 0) {
                System.out.println("Usage:");
                System.out.println("  create --description <text> [--parentId <id>]");
                System.out.println("  update --id <issueId> --status <OPEN|IN_PROGRESS|CLOSED>");
                System.out.println("  list --status <OPEN|IN_PROGRESS|CLOSED>");
                return;
            }

            String command = args[0].toLowerCase();
            Map<String, String> params = parseArgs(args);

            switch (command) {
                case "create" -> {
                    String description = params.get("description");
                    String parentId = params.get("parentId");

                    if (description == null || description.isBlank()) {
                        System.out.println("Missing required --description parameter");
                        return;
                    }

                    issueService.createIssue(description, parentId);
                    System.out.println("Issue created in Google Sheets!");
                }
                case "update" -> {
                    String id = params.get("id");
                    String status = params.get("status");

                    if (id == null || id.isBlank() || status == null || status.isBlank()) {
                        System.out.println("Missing required --id and --status parameters");
                        return;
                    }

                    boolean updated = issueService.updateStatus(id, status);

                    if (updated) {
                        System.out.println("Issue " + id + " updated to status " + status);
                    } else {
                        System.out.println("Issue with ID " + id + " not found");
                    }
                }
                case "list" -> {
                    String status = params.get("status");
                    if (status == null || status.isBlank()) {
                        System.out.println("Missing required --status parameter");
                        return;
                    }

                    var issues = issueService.listByStatus(status);
                    if (issues.isEmpty()) {
                        System.out.println("No issues found with status: " + status);
                    } else {
                        issues.forEach(issue -> System.out.printf(
                                "ID=%s | Description=%s | ParentID=%s | Status=%s | CreatedAt=%s | UpdatedAt=%s%n",
                                issue.getId(),
                                issue.getDescription(),
                                issue.getParentId(),
                                issue.getStatus(),
                                issue.getCreatedAt(),
                                issue.getUpdatedAt()
                        ));
                    }
                }
                default -> System.out.println("Unknown command: " + command);
            }
        };
    }

    /** Helper method to parse CLI arguments (--key value). */
    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    params.put(key, args[i + 1]);
                    i++;
                } else {
                    params.put(key, null);
                }
            }
        }
        return params;
    }
}
