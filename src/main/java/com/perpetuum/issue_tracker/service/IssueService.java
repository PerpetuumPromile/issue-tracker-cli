package com.perpetuum.issue_tracker.service;

import java.time.LocalDateTime;
import java.util.List;

import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.model.Status;
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

    public boolean updateStatus(String issueId, String status) {
        Status s = Status.fromString(status); // validácia
        return repository.updateStatus(issueId, s.name());
    }

    public List<Issue> listByStatus(String status) {
        Status s = Status.fromString(status);
        return repository.findByStatus(s.name());
    }

    private String generateId() {
        String datePart = java.time.LocalDate.now().toString().replace("-", "");
        int random = (int) (Math.random() * 10000); // malé číslo na odlíšenie
        return "AD-" + datePart + "-" + random;
    }
}
