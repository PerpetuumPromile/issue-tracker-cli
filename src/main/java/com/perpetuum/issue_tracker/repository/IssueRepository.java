package com.perpetuum.issue_tracker.repository;

import com.perpetuum.issue_tracker.model.Issue;

import java.util.List;

public interface IssueRepository {
    void create(Issue issue);
    void updateStatus(String issueId, String status);
    List<Issue> findByStatus(String status);
}
