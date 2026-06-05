package net.microfalx.bootstrap.cli.command;

import net.microfalx.lang.*;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Scanner;

/**
 * Base class for all commands
 */
public abstract class Command implements Identifiable<String>, Nameable, Descriptable {

    protected final static CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    private String id;

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
     * Reads a line from the input stream.
     *
     * @return the line
     */
    public final String readLine() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    /**
     * Returns the writer of the console.
     *
     * @return a non-null instance
     * @throws IOException if an I/O error occurs
     */
    public final PrintWriter getWriter() throws IOException {
        return new PrintWriter(System.out, true);
    }

    /**
     * Prints a line to the console, no new line after.
     *
     * @param message the message to print
     * @throws IOException if an I/O error occurs
     */
    public final Command print(String message) throws IOException {
        return writeAndFlush(message);
    }

    /**
     * Prints a line to the console and adds a new line after.
     *
     * @param message the message to print
     * @throws IOException if an I/O error occurs
     */
    public final Command printLn(String message) throws IOException {
        return writeAndFlush(message + "\n");
    }

    /**
     * Returns the working directory for the command.
     *
     * @return a non-null instance
     */
    protected final File getWorkingDirectory() {
        return JvmUtils.getWorkingDirectory();
    }

    private Command writeAndFlush(String message) throws IOException {
        PrintWriter writer = getWriter();
        writer.write(message);
        writer.flush();
        return this;
    }

    private CommandLine.Command getCommandAnnotation() {
        CommandLine.Command commandAnnot = AnnotationUtils.getAnnotation(this, CommandLine.Command.class);
        if (commandAnnot == null) {
            throw new IllegalStateException("A command class (" + ClassUtils.getName(this) + ") must be annotated with @Command");
        }
        return commandAnnot;
    }
}
