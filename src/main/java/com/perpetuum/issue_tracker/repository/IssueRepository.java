package com.perpetuum.issue_tracker.repository;

import java.util.List;

import com.perpetuum.issue_tracker.model.Issue;

public interface IssueRepository {
    void create(Issue issue);

    boolean updateStatus(String issueId, String status);

    List<Issue> findByStatus(String status);
}
