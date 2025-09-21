package com.perpetuum.issue_tracker.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    private String id;
    private String description;
    private String parentId;
    private String status;          // OPEN, IN_PROGRESS, CLOSED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
