package com.perpetuum.issue_tracker.service;

import java.time.LocalDateTime;
import java.util.List;

import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.model.Status;
import com.perpetuum.issue_tracker.repository.IssueRepository;

/**
 * Application service layer for managing issues.
 *
 * Responsibilities:
 * - Orchestrates business logic for creating, updating, and listing issues.
 * - Validates inputs before delegating persistence to the repository.
 * - Enforces domain rules such as requiring a description and valid status.
 *
 */
public class IssueService {

    private final IssueRepository repository;

    public IssueService(IssueRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new issue with OPEN status and persists it.
     *
     * @param description issue description (required, non-blank)
     * @param parentId optional parent issue ID
     * @throws IllegalArgumentException if description is null or blank
     */
    public void createIssue(String description, String parentId) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        Issue issue = Issue.builder()
                .id(generateId())
                .description(description)
                .parentId(parentId)
                .status(Status.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        repository.create(issue);
    }

    /**
     * Updates the status of an existing issue.
     *
     * @param issueId ID of the issue to update
     * @param status new status (validated against Status enum)
     * @return true if updated, false if issue not found
     */
    public boolean updateStatus(String issueId, String status) {
        Status s = Status.fromString(status);
        return repository.updateStatus(issueId, s);
    }

    /**
     * Lists issues filtered by status.
     *
     * @param status filter value (validated against Status enum)
     * @return list of issues with the given status
     */
    public List<Issue> listByStatus(String status) {
        Status s = Status.fromString(status);
        return repository.findByStatus(s);
    }

    /**
     * Generates a unique issue ID.
     *  Format: AD-XXXXXXXX
    * - A random UUID is used to ensure uniqueness.
    */
    private String generateId() {
        String uuidPart = java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        return "AD-" + uuidPart;
    }
}
