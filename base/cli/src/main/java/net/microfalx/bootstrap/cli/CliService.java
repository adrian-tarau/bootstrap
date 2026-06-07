package net.microfalx.bootstrap.cli;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.application.Application;
import net.microfalx.bootstrap.cli.command.Command;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Slf4j
@Service
public class CliService implements InitializingBean {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private Application application;

    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    /**
     * Returns the registered commands.
     *
     * @return a non-null instance
     */
    public Collection<Command> getCommands() {
        List<Command> commandList = new ArrayList<>(commands.values());
        commandList.sort(Comparator.comparing(Command::getName));
        return commandList;
    }

    /**
     * Returns a command by identifier or name.
     *
     * @param idOrName the identifier or name
     * @return a non-null instance
     */
    public Command getCommand(String idOrName) {
        requireNotEmpty(idOrName);
        Command command = commands.getOrDefault(StringUtils.toIdentifier(idOrName), commands.get(idOrName));
        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + idOrName);
        }
        return command;
    }

    /**
     * Executes a command.
     *
     * @param args the arguments
     * @return the return code
     */
    public int execute(String... args) {
        LOGGER.info("Execute command: {}", Arrays.toString(args));
        CommandLine commandLine = new CommandLine(createRootCommand());
        commandLine.setExecutionExceptionHandler(new CiExceptionHandling());
        return commandLine.execute(args);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverCommands();
    }

    private void discoverCommands() {
        String[] beanNames = applicationContext.getBeanNamesForType(Command.class);
        for (String beanName : beanNames) {
            Command command = (Command) applicationContext.getBean(beanName);
            commands.put(command.getName(), command);
            commands.put(command.getId(), command);
        }
    }

    private CommandLine.Model.CommandSpec createRootCommand() {
        CommandLine.Model.CommandSpec standardHelpOptions = CommandLine.Model.CommandSpec.create()
                .addOption(CommandLine.Model.OptionSpec.builder("-h", "--help")
                        .usageHelp(true)
                        .description("Show this help message and exit.").build())
                .addOption(CommandLine.Model.OptionSpec.builder("-V", "--version")
                        .versionHelp(true)
                        .description("Print version information and exit.").build());

        CommandLine.Model.CommandSpec root = CommandLine.Model.CommandSpec.create()
                .name(application.getExecutable())
                .version(application.getVersion())
                .addMixin("standardHelpOptions", standardHelpOptions);
        for (Command command : commands.values()) {
            root.addSubcommand(command.getName(), CommandLine.Model.CommandSpec.forAnnotatedObject(command));
        }
        return root;
    }

    private void listCommands() {
        String commands = getCommands().stream().map(Command::getName).sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining(", "));
        LOGGER.info("Registered commands: " + commands);
    }
}
