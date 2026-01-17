package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.*;

import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Getter
@ToString
public class Issue implements Identifiable<String>, Nameable, Descriptable, Cloneable {

    private final String id;
    private final String name;
    private final Type type;
    private String description;
    private LocalDateTime firstDetectedAt = LocalDateTime.now();
    private LocalDateTime lastDetectedAt = firstDetectedAt;
    private int occurrences = 1;
    private Severity severity = Severity.MEDIUM;

    public static Issue create(Type type, String name) {
        return create(type, name, null);
    }

    public static Issue create(Type type, String name, String description) {
        String id = StringUtils.toIdentifier(type.name().toLowerCase() + "_" + name);
        return new Issue(type, id, name, null);
    }

    private Issue(Type type, String id, String name, String description) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        this.type = type;
        this.id = id;
        this.name = name;
        this.description = description;
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
        SECURITY,
        PERFORMANCE,
        STABILITY,
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
