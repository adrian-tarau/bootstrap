package net.microfalx.bootstrap.cli;

import net.microfalx.bootstrap.cli.command.Command;
import net.microfalx.bootstrap.test.ServiceIntegrationTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = CliService.class)
public class CliServiceIntegrationTest extends ServiceIntegrationTestCase {

    @Autowired CliService cliService;

    @Test
    void getCommands() {
        assertEquals(1, cliService.getCommands().size());
    }

    @Test
    void getCommand() {
        Command command = cliService.getCommand("version");
        assertEquals("version", command.getName());
        assertEquals("Displays the application version", command.getDescription());
    }

    @Test
    void askHelp() {
        cliService.execute("-h");
        cliService.execute("--help");
    }

    @Test
    void askVersion() {
        cliService.execute("-V");
        cliService.execute("--version");
    }

    @Test
    void versionCommand() {
        cliService.execute("version");
    }
}
