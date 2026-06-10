package net.microfalx.bootstrap.cli.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ExecutionTest {

    private TestTool tool;
    private TestExecution execution;

    @BeforeEach
    void setup() {
        tool = new TestTool();
        execution = new TestExecution(tool, "test");
        execution.setLauncher(tool.createLauncher());
    }

    @Test
    void execute() {
        assertEquals(0, execution.waitFor());
    }

    @Test
    void getLogs() {
        assertEquals(0, execution.waitFor());
        assertEquals("", execution.getLogs());
        assertEquals(0, execution.getLogsStream().count());
    }

}