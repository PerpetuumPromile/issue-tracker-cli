package com.perpetuum.issue_tracker.repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.perpetuum.issue_tracker.infrastructure.GoogleSheetsFacade;
import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.model.Status;

/**
 * GoogleSheetsIssueRepository
 *
 * Repository implementation for persisting issues into Google Sheets.
 * It maps Issue objects into rows and vice versa.
 */
public class GoogleSheetsIssueRepository implements IssueRepository {

    private final GoogleSheetsFacade sheetsFacade;

    // Column indices (0-based)
    private static final int COL_ID = 0;
    private static final int COL_DESCRIPTION = 1;
    private static final int COL_PARENT_ID = 2;
    private static final int COL_STATUS = 3;
    private static final int COL_CREATED_AT = 4;
    private static final int COL_UPDATED_AT = 5;

    /**
     * Custom runtime exception to encapsulate repository-specific errors.
     */
    public static class IssueRepositoryException extends RuntimeException {
        public IssueRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public GoogleSheetsIssueRepository(GoogleSheetsFacade sheetsFacade) {
        this.sheetsFacade = sheetsFacade;
    }

    /**
     * Append a new Issue as a row in Google Sheets.
     */
    @Override
    public void create(Issue issue) {
        try {
            sheetsFacade.appendRow(Arrays.asList(
                    issue.getId(),
                    issue.getDescription(),
                    issue.getParentId() != null ? issue.getParentId() : "",
                    issue.getStatus().name(),
                    issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "",
                    issue.getUpdatedAt() != null ? issue.getUpdatedAt().toString() : ""
            ));
        } catch (IOException e) {
            throw new IssueRepositoryException(
                    String.format("Failed to create issue [%s] in Google Sheets", issue.getId()), e);
        }
    }

    /**
     * Update the status of an existing Issue by its ID.
     * Reads all rows, finds the one matching issueId, updates status + timestamp.
     */
    @Override
    public boolean updateStatus(String issueId, Status status) {
        try {
            List<List<Object>> values = sheetsFacade.readAll();
            if (values == null || values.isEmpty()) {
                return false;
            }

            for (int i = 1; i < values.size(); i++) { // skip header row
                List<Object> row = values.get(i);
                if (row.get(COL_ID).toString().equals(issueId)) {
                    row.set(COL_STATUS, status.name());

                    // Ensure row has enough columns
                    while (row.size() <= COL_UPDATED_AT) {
                        row.add("");
                    }
                    row.set(COL_UPDATED_AT, LocalDateTime.now().toString());

                    sheetsFacade.updateRow(i + 1, row); // Sheets rows are 1-based
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new IssueRepositoryException(
                    String.format("Failed to update status for issue [%s] in Google Sheets", issueId), e);
        }
    }

    /**
     * Find all issues by status.
     * Reads all rows, maps them into Issue objects, filters by status.
     */
    @Override
    public List<Issue> findByStatus(Status status) {
        try {
            List<List<Object>> values = sheetsFacade.readAll();
            if (values == null || values.isEmpty()) {
                return List.of();
            }

            return values.stream()
                    .skip(1) // skip header row
                    .map(row -> Issue.builder()
                            .id(row.get(COL_ID).toString())
                            .description(row.get(COL_DESCRIPTION).toString())
                            .parentId(row.size() > COL_PARENT_ID ? row.get(COL_PARENT_ID).toString() : null)
                            .status(row.size() > COL_STATUS
                                    ? Status.fromString(row.get(COL_STATUS).toString())
                                    : null)
                            .createdAt(row.size() > COL_CREATED_AT
                                    ? LocalDateTime.parse(row.get(COL_CREATED_AT).toString())
                                    : null)
                            .updatedAt(row.size() > COL_UPDATED_AT
                                    ? LocalDateTime.parse(row.get(COL_UPDATED_AT).toString())
                                    : null)
                            .build())
                    .filter(issue -> status.equals(issue.getStatus()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IssueRepositoryException(
                    String.format("Failed to fetch issues with status [%s] from Google Sheets", status), e);
        }
    }
}
