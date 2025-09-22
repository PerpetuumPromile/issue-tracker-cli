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
                System.out.println("Usage:");
                System.out.println("  create --description <text> [--parentId <id>]");
                System.out.println("  update --id <issueId> --status <OPEN|IN_PROGRESS|CLOSED>");
                System.out.println("  list --status <OPEN|IN_PROGRESS|CLOSED>");
                return;
            }

            String command = args[0].toLowerCase();

            switch (command) {
                case "create" -> {
                    Map<String, String> params = parseArgs(args);

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
                    Map<String, String> params = parseArgs(args);

                    String issueId = params.get("id");
                    String status = params.get("status");

                    if (issueId == null || status == null) {
                        System.out.println("❌ Missing required parameters: --id and --status");
                        return;
                    }

                    try {
                        issueService.updateStatus(issueId, status);
                        System.out.println("🔄 Issue " + issueId + " updated to status: " + status);
                    } catch (RuntimeException ex) {
                        System.out.println("❌ " + ex.getMessage()); // zachytí „Issue with ID ... not found“
                    }
                }
                case "list" -> {
                    Map<String, String> params = parseArgs(args);

                    String status = params.get("status");
                    if (status == null || status.isBlank()) {
                        System.out.println("❌ Missing required --status parameter");
                        return;
                    }

                    var issues = issueService.listByStatus(status);
                    if (issues.isEmpty()) {
                        System.out.println("ℹ️ No issues found with status: " + status);
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
                default -> System.out.println("❌ Unknown command: " + command);
            }
        };
    }

    // 🛠️ Pomocná metóda na parsovanie argumentov (--key value)
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
