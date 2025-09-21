package com.perpetuum.issue_tracker;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.perpetuum.issue_tracker.infrastructure.GoogleSheetsFacade;
import com.perpetuum.issue_tracker.repository.GoogleSheetsIssueRepository;
import com.perpetuum.issue_tracker.service.IssueService;

@SpringBootApplication
public class IssueTrackerApplication {

    // ✅ SpreadsheetId sa teraz načítava z application.properties
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    public static void main(String[] args) {
        SpringApplication.run(IssueTrackerApplication.class, args);
    }

    // ✅ Najprv definujeme facade ako bean
    @Bean
    public GoogleSheetsFacade googleSheetsFacade() throws Exception {
        GoogleSheetsFacade facade = new GoogleSheetsFacade(spreadsheetId);

        // 🟢 Inicializujeme hlavičku pri štarte
        facade.initializeHeaderIfEmpty();

        return facade;
    }

    // ✅ Repo bean
    @Bean
    public GoogleSheetsIssueRepository issueRepository(GoogleSheetsFacade facade) {
        return new GoogleSheetsIssueRepository(facade);
    }

    // ✅ Service bean
    @Bean
    public IssueService issueService(GoogleSheetsIssueRepository repository) {
        return new IssueService(repository);
    }

    // ✅ CLI runner
    @Bean
    public CommandLineRunner commandLineRunner(IssueService issueService) {
        return args -> {
            System.out.println("✅ Issue Tracker CLI running...");

            if (args.length == 0) {
                System.out.println("Usage: create <description> | update <id> <status> | list <status>");
                return;
            }

            String command = args[0].toLowerCase();

            switch (command) {
                case "create" -> {
                    // Preparsuj argumenty do mapy
                    Map<String, String> params = new HashMap<>();
                    for (int i = 1; i < args.length; i++) {
                        if (args[i].startsWith("--")) {
                            String key = args[i].substring(2); // odstránime "--"
                            if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                                params.put(key, args[i + 1]);
                                i++; // posuň sa o jeden argument dopredu
                            } else {
                                params.put(key, null); // flag bez hodnoty
                            }
                        }
                    }

                    String description = params.get("description");
                    String parentId = params.get("parentId");

                    if (description == null || description.isBlank()) {
                        System.out.println("❌ Missing required --description parameter");
                        return;
                    }

                    issueService.createIssue(description, parentId);
                    System.out.println("📌 Issue created in Google Sheets!");
                }
                case "update" -> {
                    System.out.println("🔄 Update not implemented yet.");
                }
                case "list" -> {
                    System.out.println("📋 List not implemented yet.");
                }
                default -> System.out.println("❌ Unknown command: " + command);
            }
        };
    }
}
