package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.*;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.formatMessage;
import static net.microfalx.lang.TextUtils.abbreviateMiddle;

@Getter
@ToString
public class Issue implements Identifiable<String>, Nameable, Descriptable, Cloneable {

    private String id;
    private final String name;
    private final Type type;
    private String module = "-";
    private String description;
    private LocalDateTime firstDetectedAt = LocalDateTime.now();
    private LocalDateTime lastDetectedAt = firstDetectedAt;
    private int occurrences = 1;
    private Severity severity = Severity.MEDIUM;
    private Class<?> throwableClass;

    static final Queue<Issue> ISSUES = new ConcurrentLinkedQueue<>();

    public static Issue create(Type type, String name) {
        return new Issue(type, name);
    }

    public static Issue create(Type type, String... names) {
        return new Issue(type, abbreviateMiddle(String.join(" : ", names), 60));
    }

    private Issue(Type type, String name) {
        requireNotEmpty(type);
        requireNotEmpty(name);
        this.type = type;
        this.name = name;
        updateId();
    }

    /**
     * Changes the module of this issue.
     *
     * @param module the new module
     * @return a new instance with the updated description
     */
    public Issue withModule(String module) {
        requireNotEmpty(module);
        Issue copy = copy();
        copy.module = module;
        copy.updateId();
        return copy;
    }

    /**
     * Changes the description of this issue.
     *
     * @param description the new description
     * @return a new instance with the updated description
     */
    public Issue withDescription(String description) {
        Issue copy = copy();
        copy.description = description;
        return copy;
    }

    /**
     * Changes the description of this issue.
     *
     * @param pattern the new pattern for the description
     * @param args    the arguments passed to format the description
     * @return a new instance with the updated description
     */
    public Issue withDescription(String pattern, Object... args) {
        Issue copy = copy();
        copy.description = formatMessage(pattern, args);
        return copy;
    }

    /**
     * Changes the description of this issue including the root cause message of the exception.
     *
     * @param pattern   the new pattern for the description
     * @param args      the arguments passed to format the description
     * @param throwable the exception
     * @return a new instance with the updated description
     */
    public Issue withDescription(Throwable throwable, String pattern, Object... args) {
        Issue copy = copy();
        copy.description = formatMessage(pattern, args) + ", root cause: " + getRootCauseMessage(throwable);
        copy.throwableClass = throwable != null ? throwable.getClass() : null;
        return copy;
    }

    /**
     * Changes the severity of this issue.
     *
     * @param severity the new severity
     * @return a new instance with the updated severity
     */
    public Issue withSeverity(Severity severity) {
        requireNonNull(severity);
        Issue copy = copy();
        copy.severity = severity;
        return copy;
    }

    /**
     * Changes the occurrences of this issue.
     *
     * @param occurrences the new occurrences
     * @return a new instance with the updated occurrences
     */
    public Issue withOccurrences(Integer occurrences) {
        Issue copy = copy();
        copy.occurrences = occurrences;
        return copy;
    }

    /**
     * Changes the occurrences of this issue.
     *
     * @param detectedAt the new occurrences
     * @return a new instance with the updated occurrences
     */
    public Issue withDetectedAt(LocalDateTime detectedAt) {
        Issue copy = copy();
        if (copy.firstDetectedAt == null) copy.firstDetectedAt = detectedAt;
        copy.lastDetectedAt = detectedAt;
        copy.occurrences = copy.occurrences + 1;
        return copy;
    }

    /**
     * Registers this issue for reporting.
     */
    public void register() {
        ISSUES.offer(this);
    }

    private void updateId() {
        Hashing hashing = Hashing.create();
        hashing.update(type.name().toLowerCase());
        hashing.update(StringUtils.toIdentifier(module));
        hashing.update(StringUtils.toIdentifier(name));
        if (throwableClass != null) hashing.update(throwableClass.getName());
        this.id = hashing.asString();
    }

    private Issue copy() {
        try {
            return (Issue) clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    /**
     * An enum representing the type of the issue.
     */
    public enum Type {

        /**
         * Vulnerabilities, auth issues, data leaks, misconfigurations
         */
        SECURITY,

        /**
         * Slow responses, high latency, excessive resource usage
         */
        PERFORMANCE,

        /**
         * Downtime, service interruptions, network issues
         */
        AVAILABILITY,

        /**
         * DNS issues, firewall blocks, routing problems, service availability
         */
        CONNECTIVITY,

        /**
         * Crashes, deadlocks, unhandled exceptions, memory leaks
         */
        STABILITY,

        /**
         * Bad configuration/variables, secrets, feature flags, misconfigured limits
         */
        CONFIGURATION,

        /**
         * Missing metrics, logs, traces, poor visibility
         */
        OBSERVABILITY,

        /**
         * Corrupted data, duplicates, missing records
         */
        DATA_INTEGRITY
    }

    /**
     * A severity associated with the issue.
     */
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
