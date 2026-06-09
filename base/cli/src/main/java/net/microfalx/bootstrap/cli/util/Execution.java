package net.microfalx.bootstrap.cli.util;

import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.Nameable;

import java.io.IOException;
import java.util.stream.Stream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

/**
 * Base class for all tool executions.
 *
 * @param <T> the tool type
 * @param <E> the execution type
 */
public abstract class Execution<T extends Tool<T>, E extends Execution<T, E>> implements Nameable {

    private final T tool;
    private final String name;

    private ProcessLauncher launcher;

    public Execution(T tool, String name) {
        requireNonNull(tool);
        requireNonNull(name);
        this.tool = tool;
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * Returns the build tool.
     *
     * @return a non-null instance
     */
    public final T getTool() {
        return tool;
    }

    /**
     * Attaches a process to this execution.
     *
     * @param launcher the process launcher
     * @return self
     */
    public final E setLauncher(ProcessLauncher launcher) {
        this.launcher = requireNonNull(launcher);
        return self();
    }

    /**
     * Returns the console associated with the build execution.
     *
     * @return a non-null instance
     */
    public final Console getConsole() {
        return tool.getConsole();
    }

    /**
     * Waits for the execution to complete and returns the exit code.
     *
     * @return a positive integer, 0 = OK
     */
    public int waitFor() {
        return doGetLauncher().waitFor();
    }

    /**
     * Returns the execution date.
     *
     * @return a non-null string
     */
    public final String getLogs() {
        try {
            return doGetLauncher().getLogs().loadAsString();
        } catch (IOException e) {
            return "#ERROR: " + getRootCauseDescription(e);
        }
    }

    /**
     * Returns a stream of string (lines) from the process log.
     *
     * @return a non-null instance
     */
    public final Stream<String> getLogsStream() {
        return doGetLauncher().getLogsStream();
    }

    protected final ProcessLauncher doGetLauncher() {
        if (launcher == null) {
            throw new IllegalStateException("A process launcher has not been set");
        }
        return launcher;
    }

    @SuppressWarnings("unchecked")
    protected final E self() {
        return (E) this;
    }
}
