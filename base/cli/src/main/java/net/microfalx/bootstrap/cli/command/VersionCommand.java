package net.microfalx.bootstrap.cli.command;

import net.microfalx.bootstrap.application.Application;
import net.microfalx.bootstrap.cli.util.Console;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.IOException;

@Component
@CommandLine.Command(name = "version", mixinStandardHelpOptions = true,
        description = "Displays the application version")
public class VersionCommand extends RunnableCommand {

    @Autowired private Application application;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Displays verbose information")
    private boolean verbose;

    @Override
    protected void execute() throws IOException {
        Console console = getConsole();
        console.printLn(application.getName() + " - " + application.getDescription());
        console.printLn("Version: " + application.getVersion() + ", Build Number: " + application.getBuildNumber() + ", Build Time: " + application.getBuildTime());
    }
}
