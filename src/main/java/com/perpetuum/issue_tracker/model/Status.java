package com.perpetuum.issue_tracker.model;

public enum Status {
    OPEN,
    IN_PROGRESS,
    CLOSED;

    public static Status fromString(String value) {
        for (Status s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException(
            "❌ Invalid status: " + value + " (allowed: OPEN, IN_PROGRESS, CLOSED)"
        );
    }
}
