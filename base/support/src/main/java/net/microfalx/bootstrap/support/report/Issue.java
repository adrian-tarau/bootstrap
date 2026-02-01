package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.*;
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
    private Map<String, Object> attributes;
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
     * Returns the attributes associated with this issue.
     *
     * @return a non-null instance
     */
    public Map<String, Object> getAttributes() {
        return attributes != null ? unmodifiableMap(attributes) : emptyMap();
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
     * Adds an attribute to this issue.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     * @return a new instance with the added attribute
     */
    public Issue withAttribute(String name, Object value) {
        requireNotEmpty(name);
        Issue copy = copy();
        copy.attributes = new HashMap<>(copy.attributes != null ? copy.attributes : emptyMap());
        copy.attributes.put(name, merge(copy.attributes.get(name), value));
        return copy;
    }

    /**
     * Adds a counter attribute to this issue.
     *
     * @param name the name of the attribute
     * @return a new instance with the added attribute
     */
    public Issue withAttributeCounter(String name) {
        return withAttribute(name, 1);
    }

    /**
     * Merges this issue with another one.
     *
     * @param other the other issue
     * @return a new instance representing the merged issue
     */
    public Issue merge(Issue other) {
        Issue copy = copy();
        if (other.firstDetectedAt.isBefore(copy.firstDetectedAt)) {
            copy.firstDetectedAt = other.firstDetectedAt;
        }
        if (other.lastDetectedAt.isAfter(copy.lastDetectedAt)) {
            copy.lastDetectedAt = other.lastDetectedAt;
        }
        copy.occurrences = copy.occurrences + other.occurrences;
        copy.attributes = merge(copy.attributes, other.attributes);
        return copy;
    }

    /**
     * Registers this issue for reporting.
     */
    public void register() {
        ISSUES.offer(this);
    }

    /**
     * Returns a description of attributes.
     *
     * @return a non-null instance
     */
    public String getAttributesDescription() {
        if (attributes == null || attributes.isEmpty()) return EMPTY_STRING;
        StringBuilder builder = new StringBuilder();
        attributes.forEach((k, v) -> builder.append(k).append("=").append(v).append(", "));
        if (builder.length() > 2) builder.setLength(builder.length() - 2);
        return defaultIfEmpty(builder.toString(), NA_STRING);
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

    private Map<String, Object> merge(Map<String, Object> currentMap, Map<String, Object> newMap) {
        if (currentMap == null || newMap == null) return null;
        Map<String, Object> merged = new java.util.HashMap<>(currentMap);
        newMap.forEach((k, v) -> merged.put(k, merge(merged.get(k), v)));
        return merged;
    }

    private Object merge(Object previous, Object current) {
        if (previous instanceof Number previousNumber && current instanceof Number newNumber) {
            if (previousNumber instanceof Double || newNumber instanceof Double) {
                return previousNumber.doubleValue() + newNumber.doubleValue();
            } else if (previousNumber instanceof Float || newNumber instanceof Float) {
                return previousNumber.floatValue() + newNumber.floatValue();
            } else if (previousNumber instanceof Long || newNumber instanceof Long) {
                return previousNumber.longValue() + newNumber.longValue();
            } else {
                return previousNumber.intValue() + newNumber.intValue();
            }
        }
        return current;
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
         * Denial of Service, excessive requests, abuse
         */
        DOS,

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

        /**
         * Notice level issue - informational only
         */
        NOTICE,

        /**
         * Low severity issue - minor impact
         */
        LOW,

        /**
         * Medium severity issue - moderate impact
         */
        MEDIUM,

        /**
         * High severity issue - significant impact
         */
        HIGH,

        /**
         * Critical severity issue - severe impact
         */
        CRITICAL
    }
}
