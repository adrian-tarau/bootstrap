package net.microfalx.bootstrap.core.process;

import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.*;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.lang.IOUtils.*;
import static net.microfalx.lang.JvmUtils.getLogsDirectory;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.ThreadUtils.sleepMillis;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * A class which can execute an external process.
 */
@Slf4j
public class ProcessLauncher implements Identifiable<String>, Nameable {

    private final static AtomicInteger LOG_COUNTER = new AtomicInteger(1);

    private final String id;
    private String name;
    private final File executable;
    private final List<String> arguments = new ArrayList<>();
    private ThreadPool threadPool = ThreadPool.get();
    private Process process;
    private File logFile;
    private Duration startupTimeout = Duration.ofSeconds(5);
    private Duration executionTimeout = Duration.ofMinutes(5);
    private File workingDirectory;
    private boolean dryRun;
    private boolean startupFailed;
    private boolean inheritEnvironment = true;
    private volatile int exitCode;
    private final Map<String, Object> environment = new HashMap<>();
    private volatile boolean started;
    private volatile boolean running;
    private volatile long lastPing;

    public static ProcessLauncher create(String executable) {
        requireNotEmpty(executable);
        return create(new File(executable));
    }

    public static ProcessLauncher create(File executable) {
        return new ProcessLauncher(executable);
    }

    ProcessLauncher(File executable) {
        requireNonNull(executable);
        this.executable = executable;
        this.id = toIdentifier(executable.getName());
        this.name = capitalizeFirst(executable.getName());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Changes the name associated with this instance.
     *
     * @param name the new name
     * @return self
     */
    public ProcessLauncher setName(String name) {
        requireNotEmpty(name);
        this.name = name;
        return this;
    }

    /**
     * Adds a new argument for the process.
     *
     * @param argument the argument
     * @return self
     */
    public ProcessLauncher addArgument(String argument) {
        arguments.add(requireNonNull(argument));
        return this;
    }

    /**
     * Adds a new environment variable for the process.
     *
     * @param name  the name
     * @param value the value
     * @return self
     */
    public ProcessLauncher addEnvironment(String name, String value) {
        environment.put(name, requireNonNull(value));
        return this;
    }

    /**
     * Changes the working directory.
     *
     * @param workingDirectory the new working directory
     * @return self
     */
    public ProcessLauncher setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = requireNonNull(workingDirectory);
        return this;
    }

    /**
     * Changes the thread pool.
     *
     * @param threadPool the thread pool
     * @return self
     */
    public ProcessLauncher setThreadPool(ThreadPool threadPool) {
        this.threadPool = requireNonNull(threadPool);
        return this;
    }

    /**
     * Changes the startup timeout.
     *
     * @param timeout the new timeout
     * @return self
     */
    public ProcessLauncher setStartupTimeout(Duration timeout) {
        this.startupTimeout = requireNonNull(timeout);
        return this;
    }

    /**
     * Changes the execution timeout.
     *
     * @param timeout the new timeout
     * @return self
     */
    public ProcessLauncher setExecutionTimeout(Duration timeout) {
        this.executionTimeout = requireNonNull(timeout);
        return this;
    }

    /**
     * Changes whether the process execution is simulated.
     *
     * @param dryRun {@code true} for dry run, {@code false} otherwise
     * @return self
     */
    public ProcessLauncher setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    /**
     * Indicates whether the environment of the current process passed to the child process.
     * <p>
     * By default, all child processes will inherit the environment.
     *
     * @param inheritEnvironment {@code true} to inherit the environment
     * @return self
     */
    public ProcessLauncher setInheritEnvironment(boolean inheritEnvironment) {
        this.inheritEnvironment = inheritEnvironment;
        return this;
    }

    /**
     * Returns whether the server is idle (no chat communication).
     *
     * @return {@code true} if the server is idle, {@code false} otherwise
     */
    public boolean isIdle() {
        return millisSince(lastPing) > ONE_MINUTE;
    }

    /**
     * Returns whether the process was started.
     *
     * @return {@code true} if the server is started, {@code false} otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns whether the server is available to accept new requests.
     *
     * @return {@code true} if the server is available, {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the logs of the server, including both standard output and standard error.
     *
     * @return a non-null instance
     */
    public Resource getLogs() {
        if (dryRun) {
            return Resource.text("Executed: " + String.join(" ", buildCommandLine()));
        } else if (logFile != null) {
            StringBuilder builder = new StringBuilder();
            append(builder, logFile);
            return Resource.text(builder.toString());
        } else {
            return Resource.text(EMPTY_STRING);
        }
    }

    /**
     * Returns the logs as a String.
     *
     * @return a non-null instance
     */
    public String getLogsAsString() {
        try {
            return getLogs().loadAsString();
        } catch (IOException e) {
            return "#ERROR: " + getRootCauseDescription(e);
        }
    }

    /**
     * Returns a stream of string (lines) from the process log.
     *
     * @return a non-null instance
     */
    public Stream<String> getLogsStream() {
        if (dryRun) {
            return Stream.of(getDescription());
        } else if (logFile != null) {
            try {
                LineNumberReader reader = new LineNumberReader(getBufferedReader(new FileReader(logFile)));
                return reader.lines();
            } catch (FileNotFoundException e) {
                return Stream.of((getException(logFile, e)));
            }
        } else {
            return Stream.empty();
        }
    }

    /**
     * Returns the properties of the server, including configuration and runtime information.
     *
     * @return a non-null instance
     */
    public Map<String, Object> getEnvironment() {
        return unmodifiableMap(environment);
    }

    /**
     * Waits for the process to complete (successful or not) and return the exit code.
     *
     * @return a positive integer, 0 = OK
     */
    public int waitFor() {
        if (dryRun) return 0;
        long end = currentTimeMillis() + startupTimeout.toMillis();
        while (!isStarted() && currentTimeMillis() < end && !startupFailed) {
            sleepMillis(10);
        }
        if (!isStarted()) {
            throw new ProcessException("Process '" + executable + "' failed to start, logs: " + getLogsAsString());
        }
        end = currentTimeMillis() + executionTimeout.toMillis();
        while (isRunning() && currentTimeMillis() < end) {
            sleepMillis(10);
        }
        return exitCode;
    }

    /**
     * Pings the process.
     */
    public void ping() {
        lastPing = currentTimeMillis();
    }

    /**
     * Runs the process, waits for the process to complete and returns the exit code.
     *
     * @return a positive integer
     */
    public int run() {
        start(false);
        return waitFor();
    }

    /**
     * Starts the process.
     *
     * @param async {@code true} to start it asynchronously, {@code false} otherwise
     */
    public void start(boolean async) {
        if (dryRun) {
            started = true;
            return;
        }
        logFile = new File(getLogsDirectory(), getId() + "_" + LOG_COUNTER.getAndIncrement() + "_" + FORMATTER.format(LocalDateTime.now()) + ".log");
        File workingDirectory = this.workingDirectory != null ? this.workingDirectory : JvmUtils.getWorkingDirectory();
        ProcessBuilder builder = new ProcessBuilder(buildCommandLine()).directory(workingDirectory);
        builder.redirectOutput(logFile).redirectErrorStream(true);
        if (inheritEnvironment) {
            System.getenv().forEach((k, v) -> builder.environment().put(k, ObjectUtils.toString(v)));
        }
        environment.forEach((k, v) -> builder.environment().put(k, ObjectUtils.toString(v)));
        if (async) {
            threadPool.execute(() -> doStart(builder));
        } else {
            doStart(builder);
        }
    }

    /**
     * Stops the server.
     */
    public void stop() {
        LOGGER.info("Stopping process {}", getId());
        try {
            process.destroy();
        } finally {
            running = false;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("executable", executable)
                .add("arguments", arguments)
                .add("startupTimeout", startupTimeout)
                .add("executionTimeout", executionTimeout)
                .add("workingDirectory", workingDirectory)
                .add("dryRun", dryRun)
                .add("started", started)
                .add("running", running)
                .toString();
    }

    private void append(StringBuilder builder, File file) {
        String text = null;
        if (file.exists()) text = read(file);
        text = StringUtils.trim(text);
        if (isNotEmpty(text)) builder.append(text).append("\n");
    }

    private String read(File file) {
        try {
            return getInputStreamAsString(getBufferedInputStream(file));
        } catch (Exception e) {
            return getException(file, e);
        }
    }

    private String getException(File file, Throwable throwable) {
        return "#ERROR for " + file.getAbsolutePath() + ": " + getRootCauseDescription(throwable);
    }

    private void doStart(ProcessBuilder builder) {
        try {
            process = builder.start();
            started = true;
            running = true;
        } catch (Exception e) {
            startupFailed = true;
            throw new ProcessException("Failed to run executable '" + executable + "'", e);
        }
        threadPool.execute(new Worker());
    }

    private List<String> buildCommandLine() {
        List<String> command = new ArrayList<>();
        command.add(executable.isAbsolute() ? executable.getAbsolutePath() : executable.getName());
        command.addAll(arguments);
        return command;
    }

    private String getDescription() {
        return "Executed: " + String.join(" ", buildCommandLine());
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                // ignore
            } finally {
                running = false;
            }
        }
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
}
