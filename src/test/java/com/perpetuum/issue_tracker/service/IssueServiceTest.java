package com.perpetuum.issue_tracker.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.perpetuum.issue_tracker.model.Issue;
import com.perpetuum.issue_tracker.repository.IssueRepository;

class IssueServiceTest {

    private IssueRepository repository;
    private IssueService service;

    /**
     * This method runs before each test.
     * - Creates a mock of the IssueRepository.
     * - Injects the mock into a fresh IssueService instance.
     */
    @BeforeEach
    void setup() {
        repository = mock(IssueRepository.class);
        service = new IssueService(repository);
    }

    /**
     * ✅ Positive test:
     * Ensures that when we create an issue, it:
     * - Saves with description
     * - Automatically sets status to OPEN
     * - Has a generated ID and createdAt timestamp
     */
    @Test
    void createIssue_shouldSaveIssueWithOpenStatus() {
        service.createIssue("Test issue", null);

        ArgumentCaptor<Issue> captor = ArgumentCaptor.forClass(Issue.class);
        verify(repository).create(captor.capture());

        Issue saved = captor.getValue();
        assertEquals("Test issue", saved.getDescription());
        assertEquals("OPEN", saved.getStatus());
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    /**
     * ✅ Positive test:
     * Checks that updateStatus() returns true
     * when the repository confirms the update.
     */
    @Test
    void updateStatus_shouldReturnTrueIfRepositoryUpdates() {
        when(repository.updateStatus("ISSUE-1", "CLOSED")).thenReturn(true);

        boolean result = service.updateStatus("ISSUE-1", "CLOSED");

        assertTrue(result);
        verify(repository).updateStatus("ISSUE-1", "CLOSED");
    }

    /**
     * ✅ Positive test:
     * listByStatus() should return issues filtered by status.
     */
    @Test
    void listByStatus_shouldReturnFilteredIssues() {
        Issue issue = Issue.builder()
                .id("ISSUE-1")
                .description("Desc")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByStatus("OPEN")).thenReturn(List.of(issue));

        var result = service.listByStatus("OPEN");

        assertEquals(1, result.size());
        assertEquals("ISSUE-1", result.get(0).getId());
    }

    /**
     * ❌ Negative test:
     * If description is null, createIssue() should throw an exception.
     */
    @Test
    void createIssue_shouldFailWhenDescriptionIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createIssue(null, null));
        assertEquals("Description is required", ex.getMessage());
    }

    /**
     * ❌ Negative test:
     * If repository.updateStatus() fails (returns false),
     * the service should return false as well.
     */
    @Test
    void updateStatus_shouldReturnFalseIfRepositoryFails() {
        when(repository.updateStatus("ISSUE-404", "CLOSED")).thenReturn(false);

        boolean result = service.updateStatus("ISSUE-404", "CLOSED");

        assertFalse(result);
        verify(repository).updateStatus("ISSUE-404", "CLOSED");
    }

    /**
     * Edge case:
     * If repository returns no issues, service should return an empty list.
     */
    @Test
    void listByStatus_shouldReturnEmptyListWhenNoIssues() {
        when(repository.findByStatus("CLOSED")).thenReturn(List.of());

        var result = service.listByStatus("CLOSED");

        assertTrue(result.isEmpty());
    }

    /**
     * ❌ Negative test:
     * If user passes invalid status (not OPEN/IN_PROGRESS/CLOSED),
     * service should throw IllegalArgumentException.
     */
    @Test
    void listByStatus_shouldThrowIfInvalidStatus() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.listByStatus("INVALID")
        );
        assertTrue(ex.getMessage().contains("Invalid status: INVALID"));
    }

}
