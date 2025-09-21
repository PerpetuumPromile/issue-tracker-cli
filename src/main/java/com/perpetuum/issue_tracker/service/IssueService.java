package com.perpetuum.issue_tracker.service;

import java.time.LocalDateTime;
import java.util.List;

import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.repository.IssueRepository;

public class IssueService {

    private final IssueRepository repository;

    public IssueService(IssueRepository repository) {
        this.repository = repository;
    }

    public void createIssue(String description, String parentId) {
        Issue issue = Issue.builder()
                .id(generateId())
                .description(description)
                .parentId(parentId)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        repository.create(issue);
    }

    public void updateStatus(String issueId, String status) {
        repository.updateStatus(issueId, status);
    }

    public List<Issue> listByStatus(String status) {
        return repository.findByStatus(status);
    }

    private String generateId() {
        // TODO: implement proper ID generator (e.g. AD-1, AD-2, etc.)
        return "TEMP-ID";
    }
}
