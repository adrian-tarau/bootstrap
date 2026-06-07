package net.microfalx.bootstrap.cli.command;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.*;
import net.microfalx.threadpool.ThreadPool;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TextUtils.abbreviateMiddle;
import static net.microfalx.lang.ThreadUtils.sleepMillis;

/**
 * Base class for all commands
 */
@Slf4j
public abstract class Command implements Identifiable<String>, Nameable, Descriptable {

    protected final static CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    private String id;

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
     * Prints 3 dots  to the console.
     *
     */
    public final Command printDots() {
        return print("...");
    }

    /**
     * Prints a line to the console, no new line after.
     *
     * @param message the message to print
     */
    public final Command print(String message) {
        return writeAndFlush(message);
    }

    /**
     * Moves to a new line.
     */
    public final Command printLn() {
        return printLn(EMPTY_STRING);
    }

    /**
     * Prints a line to the console and adds a new line after.
     *
     * @param message the message to print
     */
    public final Command printLn(String message) {
        return writeAndFlush(message + "\n");
    }

    protected final String exitCode(int code) {
        if (code == 0) {
            return ok("OK");
        } else {
            return failure("Failed (" + code + ")");
        }
    }

    protected final String ok(String text) {
        return ANSI.string("@|green " + defaultIfEmpty(text, NA_STRING) + "|@");
    }

    protected final String failure(String text) {
        return ANSI.string("@|red " + defaultIfEmpty(text, NA_STRING) + "|@");
    }

    protected final String bold(String text) {
        return ANSI.string("@|bold " + defaultIfEmpty(text, NA_STRING) + "|@");
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

    /**
     * Single quotes a string.
     *
     * @param value the value
     * @return a non-null instance
     */
    protected static String quote(String value) {
        return "'" + defaultIfEmpty(value, NA_STRING) + "'";
    }

    /**
     * Executed a task while printing "." in the console.
     *
     * @param runnable the runnable
     */
    protected void execute(Runnable runnable) {
        PrintDotTask task = new PrintDotTask();
        ThreadPool.get().execute(task);
        try {
            runnable.run();
        } finally {
            task.cancel();
        }
    }

    /**
     * Executed a task while printing "." in the console.
     *
     * @param supplier the supplier
     */
    protected <T> T execute(Supplier<T> supplier) {
        PrintDotTask task = new PrintDotTask();
        ThreadPool.get().execute(task);
        try {
            return supplier.get();
        } finally {
            task.cancel();
        }
    }

    private Command writeAndFlush(String message) {
        try {
            PrintWriter writer = getWriter();
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            LOGGER.warn("Failed to write message '{}'", abbreviateMiddle(message, 20));
        }
        return this;
    }

    private CommandLine.Command getCommandAnnotation() {
        CommandLine.Command commandAnnot = AnnotationUtils.getAnnotation(this, CommandLine.Command.class);
        if (commandAnnot == null) {
            throw new IllegalStateException("A command class (" + ClassUtils.getName(this) + ") must be annotated with @Command");
        }
        return commandAnnot;
    }

    private class PrintDotTask implements Runnable {

        private final AtomicBoolean running = new AtomicBoolean(true);

        private float sleep = 1000;

        @Override
        public void run() {
            while (running.get()) {
                sleepMillis(sleep);
                if (running.get()) print(".");
                sleep = (float) Math.max(10f, sleep * 1.5);
            }
        }

        void cancel() {
            running.set(false);
        }
    }
}
