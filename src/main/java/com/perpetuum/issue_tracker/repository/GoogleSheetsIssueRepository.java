package com.perpetuum.issue_tracker.repository;

import java.io.IOException;
import java.time.LocalDateTime;
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
    public boolean updateStatus(String issueId, String status) {
        try {
            List<List<Object>> values = sheetsFacade.readAll();
            if (values == null || values.isEmpty()) {
                return false;
            }

            for (int i = 1; i < values.size(); i++) { // preskoč hlavičku
                List<Object> row = values.get(i);
                if (row.get(0).toString().equals(issueId)) {
                    // nastav status
                    row.set(3, status);

                    // zabezpeč dostatočný počet stĺpcov
                    while (row.size() < 6) {
                        row.add("");
                    }
                    row.set(5, LocalDateTime.now().toString());

                    sheetsFacade.updateRow(i + 1, row);
                    return true;
                }
            }
            return false;
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
