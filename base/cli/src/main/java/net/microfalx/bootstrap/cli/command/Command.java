package net.microfalx.bootstrap.cli.command;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.cli.util.Console;
import net.microfalx.lang.*;
import picocli.CommandLine;

import java.io.File;
import java.util.Objects;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Base class for all commands
 */
@Slf4j
public abstract class Command implements Identifiable<String>, Nameable, Descriptable {

    protected final static CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    private String id;
    private Console cachedConsole;

    private File workingDirectory;

    @Override
    public final String getId() {
        if (id == null) id = StringUtils.toIdentifier(getName());
        return id;
    }

    @Override
    public final String getName() {
        return getCommandAnnotation().name();
    }

    @Override
    public final String getDescription() {
        return String.join(" ", getCommandAnnotation().description());
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(getId(), command.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Returns the console.
     *
     * @return a non-null instance
     */
    public final Console getConsole() {
        if (cachedConsole == null) cachedConsole = Console.get();
        return cachedConsole;
    }

    /**
     * Returns the working directory for the command.
     *
     * @return a non-null instance
     */
    protected final File getWorkingDirectory(String workingDirectoryOverride) {
        if (workingDirectory == null) {
            workingDirectory = JvmUtils.getWorkingDirectory();
            if (isNotEmpty(workingDirectoryOverride)) {
                File workingDirectory = new File(workingDirectoryOverride);
                if (!workingDirectory.exists()) {
                    throw new CommandException("The project directory '" + workingDirectory + "' does not exist");
                }
                this.workingDirectory = workingDirectory;
            }
        }
        return workingDirectory;
    }

    private CommandLine.Command getCommandAnnotation() {
        CommandLine.Command commandAnnot = AnnotationUtils.getAnnotation(this, CommandLine.Command.class);
        if (commandAnnot == null) {
            throw new IllegalStateException("A command class (" + ClassUtils.getName(this) + ") must be annotated with @Command");
        }
        return commandAnnot;
    }
}
