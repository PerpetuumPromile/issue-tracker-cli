package com.perpetuum.issue_tracker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.perpetuum.issue_tracker.infrastructure.GoogleSheetsFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Functional tests for the CLI.
 *
 * ⚠️ Note:
 * These tests run the Spring Boot application context,
 * but instead of connecting to real Google Sheets,
 * we use a mock GoogleSheetsFacade (via @MockBean).
 * This ensures tests remain fast, reliable,
 * and do not depend on external services.
 */
@SpringBootTest(classes = IssueTrackerApplication.class,
        properties = "spring.main.web-application-type=none")
class IssueTrackerApplicationFunctionalTest {

    @MockBean
    private GoogleSheetsFacade googleSheetsFacade; // <- mock replaces real bean

    @Autowired
    private CommandLineRunner cliRunner;

    private ByteArrayOutputStream out;

    @BeforeEach
    void setupStreams() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @BeforeEach
    void setupMocks() throws Exception {
        // Stub basic behavior so CLI can run without NullPointerExceptions
        when(googleSheetsFacade.readAll()).thenReturn(java.util.Collections.emptyList());
    }

    /**
     * ✅ Test that the CLI can start with a valid "create" command.
     * It runs the CLI runner directly and verifies success output.
     */
    @Test
    void contextLoadsAndRunsCli() throws Exception {
        cliRunner.run("create", "--description", "CLI test");
        assertTrue(out.toString().contains("Issue created"));
    }

    /**
     * ✅ Test that the CLI prints an error message when the required
     * "--description" parameter is missing.
     */
    @Test
    void cliShouldFailIfDescriptionMissing() throws Exception {
        cliRunner.run("create");
        assertTrue(out.toString().contains("Missing required --description"));
    }

    /**
     * ✅ Test that the CLI prints an error message when trying to update
     * a non-existent issue ID.
     */
    @Test
    void cliShouldReportErrorOnNonExistentId() throws Exception {
        cliRunner.run("update", "--id", "FAKE-123", "--status", "CLOSED");
        assertTrue(out.toString().contains("not found"));
    }

    /**
     * ✅ Test that the CLI handles an empty "list" result gracefully.
     */
    @Test
    void cliListShouldHandleEmptyResult() throws Exception {
        cliRunner.run("list", "--status", "CLOSED");
        assertTrue(out.toString().contains("No issues found"));
    }
}
