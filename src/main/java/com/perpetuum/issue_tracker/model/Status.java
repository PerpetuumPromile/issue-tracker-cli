package com.perpetuum.issue_tracker.model;

public enum Status {
    OPEN,
    IN_PROGRESS,
    CLOSED;

    /**
     * Parse a string into a Status enum value.
     * 
     * @param value input string (case-insensitive)
     * @return matching Status
     * @throws IllegalArgumentException if the value does not match any Status
     */
    public static Status fromString(String value) {
        for (Status s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException(
            "Invalid status: " + value + " (allowed: OPEN, IN_PROGRESS, CLOSED)"
        );
    }
}