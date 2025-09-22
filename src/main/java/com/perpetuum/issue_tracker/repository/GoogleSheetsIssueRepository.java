package com.perpetuum.issue_tracker.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.perpetuum.issue_tracker.infrastructure.GoogleSheetsFacade;
import com.perpetuum.issue_tracker.model.Issue;

public class GoogleSheetsIssueRepository implements IssueRepository {

    private final GoogleSheetsFacade sheetsFacade;

    public GoogleSheetsIssueRepository(GoogleSheetsFacade sheetsFacade) {
        this.sheetsFacade = sheetsFacade;
    }

    @Override
    public void create(Issue issue) {
        try {
        sheetsFacade.appendRow(Arrays.asList(
                issue.getId(),
                issue.getDescription(),
                issue.getParentId() != null ? issue.getParentId() : "",
                issue.getStatus(),
                issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "",
                issue.getUpdatedAt() != null ? issue.getUpdatedAt().toString() : ""
            ));
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to create issue in Google Sheets", e);
        }
    }

    @Override
    public void updateStatus(String issueId, String status) {
        try {
            List<List<Object>> values = sheetsFacade.readAll();
            if (values == null || values.isEmpty()) {
                throw new RuntimeException("❌ No issues found in sheet");
            }

            for (int i = 1; i < values.size(); i++) { // preskoč hlavičku
                List<Object> row = values.get(i);
                if (row.get(0).toString().equals(issueId)) {
                    // Update status (col 4) and updatedAt (col 6)
                    row.set(3, status); 
                    if (row.size() < 6) {
                        while (row.size() < 6) row.add(""); // doplníme prázdne hodnoty
                    }
                    row.set(5, java.time.LocalDateTime.now().toString());

                    // Zapíš späť celý riadok
                    sheetsFacade.updateRow(i + 1, row); // i+1 -> lebo indexovanie v Google Sheets je od 1
                    return;
                }
            }
            throw new RuntimeException("❌ Issue with ID " + issueId + " not found");
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to update issue in Google Sheets", e);
        }
    }

    @Override
    public List<Issue> findByStatus(String status) {
        try {
            List<List<Object>> values = sheetsFacade.readAll();
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            return values.stream()
                    .skip(1) // preskoč hlavičku
                    .map(row -> Issue.builder()
                            .id(row.get(0).toString())
                            .description(row.get(1).toString())
                            .parentId(row.size() > 2 ? row.get(2).toString() : null)
                            .status(row.size() > 3 ? row.get(3).toString() : null)
                            .build())
                    .filter(issue -> status.equalsIgnoreCase(issue.getStatus()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read issues from Google Sheets", e);
        }
    }
}
