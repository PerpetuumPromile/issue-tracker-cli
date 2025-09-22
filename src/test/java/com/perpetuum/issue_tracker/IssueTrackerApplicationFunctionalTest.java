package com.perpetuum.issue_tracker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

class IssueTrackerApplicationFunctionalTest {

    /**
     * ✅ Test that the CLI can start with a valid "create" command.
     * It runs the application with a description and verifies exit code 0 (success).
     */
    @Test
    void contextLoadsAndRunsCli() {
        int exitCode = SpringApplication.exit(
            SpringApplication.run(IssueTrackerApplication.class,
                new String[]{"create", "--description", "CLI test"})); 

        assertEquals(0, exitCode);
    }

    /**
     * ✅ Test that the CLI prints an error message when the required
     * "--description" parameter is missing. The process should still
     * exit cleanly with code 0.
     */
    @Test
    void cliShouldFailIfDescriptionMissing() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int exitCode = SpringApplication.exit(
            SpringApplication.run(IssueTrackerApplication.class,
                new String[]{"create"}));

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("Missing required --description"));
    }

    /**
     * ✅ Test that the CLI prints an error message when trying to update
     * a non-existent issue ID. The process should still exit cleanly with code 0.
     */
    @Test
    void cliShouldReportErrorOnNonExistentId() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        int exitCode = SpringApplication.exit(
            SpringApplication.run(IssueTrackerApplication.class,
                new String[]{"update", "--id", "FAKE-123", "--status", "CLOSED"}));

        assertEquals(0, exitCode);
        assertTrue(out.toString().contains("not found"));
    }

    /**
     * ✅ Test that the CLI handles an empty "list" result gracefully.
     * Listing issues with a status that has no matching issues
     * should still exit cleanly with code 0.
     */
    @Test
    void cliListShouldHandleEmptyResult() {
        int exitCode = SpringApplication.exit(
            SpringApplication.run(IssueTrackerApplication.class,
                new String[]{"list", "--status", "CLOSED"}));

        assertEquals(0, exitCode);
    }
}
