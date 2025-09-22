package com.perpetuum.issue_tracker.repository;

import java.util.List;

import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.model.Status;

/**
 * Repository interface for managing issues in a persistence layer.
 *
 * Responsibilities:
 * - Abstracts data persistence (e.g., Google Sheets, database, file system).
 * - Provides operations to create issues, update their status, 
 *   and query by status.
 */
public interface IssueRepository {

    /**
     * Persists a new issue into the storage.
     *
     * @param issue the issue to store
     */
    void create(Issue issue);

    /**
     * Updates the status of an existing issue.
     *
     * @param issueId the unique identifier of the issue
     * @param status the new status (must be validated before calling)
     * @return true if the update succeeded, false if the issue was not found
     */
    boolean updateStatus(String issueId, Status status);

    /**
     * Finds all issues with the given status.
     *
     * @param status the status to filter by
     * @return a list of matching issues, possibly empty but never null
     */
    List<Issue> findByStatus(Status status);
}
