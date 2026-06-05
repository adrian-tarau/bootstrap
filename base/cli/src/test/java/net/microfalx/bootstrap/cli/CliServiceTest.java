package net.microfalx.bootstrap.cli;

import net.microfalx.bootstrap.application.ApplicationConfiguration;
import net.microfalx.bootstrap.application.ApplicationService;
import net.microfalx.bootstrap.cli.command.Command;
import net.microfalx.bootstrap.core.async.AsynchronousConfig;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {I18nService.class, ResourceService.class, ResourceProperties.class,
        OptionalValidatorFactoryBean.class, ApplicationConfiguration.class, ApplicationService.class,
        CliService.class})
@Import({I18nProperties.class, AsynchronousConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
class CliServiceTest {

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