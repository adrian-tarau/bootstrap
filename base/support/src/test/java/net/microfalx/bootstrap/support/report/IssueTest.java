package net.microfalx.bootstrap.support.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IssueTest {

    private Issue issue;

    @BeforeEach
    void setup() {
        issue = Issue.create(Issue.Type.CONNECTIVITY, "Test");
    }

    @Test
    void create() {
        assertNotNull(issue.getId());
        assertEquals("Test", issue.getName());
        assertEquals("-", issue.getModule());
        assertEquals(Issue.Severity.MEDIUM, issue.getSeverity());
        assertNull(issue.getDescription());
        assertNotNull(issue.getFirstDetectedAt());
        assertNotNull(issue.getLastDetectedAt());
        assertEquals(1, issue.getOccurrences());
        assertEquals(0, issue.getAttributes().size());
    }

    @Test
    void withModule() {
        issue = issue.withModule("Module A");
        assertEquals("Module A", issue.getModule());
    }

    @Test
    void withSeverity() {
        issue = issue.withSeverity(Issue.Severity.CRITICAL);
        assertEquals(Issue.Severity.CRITICAL, issue.getSeverity());
    }

    @Test
    void withOccurrences() {
        issue = issue.withDetectedAt(LocalDateTime.now());
        assertEquals(2, issue.getOccurrences());
        issue = issue.withOccurrences(10);
        assertEquals(10, issue.getOccurrences());
    }

    @Test
    void withAttribute() {
        issue = issue.withAttribute("a", "a");
        assertEquals(1, issue.getAttributes().size());
        assertEquals("a", issue.getAttributes().get("a"));
        issue = issue.withAttribute("a", 1);
        assertEquals(1, issue.getAttributes().size());
        issue = issue.withAttribute("a", 1);
        assertEquals(1, issue.getAttributes().size());
        assertEquals(2, issue.getAttributes().get("a"));
    }

    @Test
    void merge() {
        issue = issue.withAttribute("a", 1);
        Issue newIssue = Issue.create(Issue.Type.AVAILABILITY, "Test")
                .withAttribute("a", 3).withAttribute("b", 3);
        issue = issue.merge(newIssue);
        assertEquals(2, issue.getAttributes().size());
        assertEquals(4, issue.getAttributes().get("a"));
        assertEquals(3, issue.getAttributes().get("b"));
    }
}