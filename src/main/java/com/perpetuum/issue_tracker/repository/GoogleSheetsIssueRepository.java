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
        // TODO: implement full update (search by ID and update row)
        throw new UnsupportedOperationException("Update not implemented yet");
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
