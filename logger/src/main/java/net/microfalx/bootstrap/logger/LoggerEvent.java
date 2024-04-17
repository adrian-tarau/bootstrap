package net.microfalx.bootstrap.logger;

import lombok.*;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A logger event, independent of the logging library.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoggerEvent implements Identifiable<Long>, Timestampable<LocalDateTime> {

    private long id;
    private String name;
    private long timestamp;
    private long sequenceNumber;
    private Level level;
    private String message;
    private String threadName;
    private Map<String, String> mdc;
    private String exceptionClassName;
    private StackTraceElement[] stackTraceElements;
    private String correlationId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(timestamp);
    }

    public String getCorrelationId() {
        if (correlationId != null) return correlationId;
        Hashing hashing = Hashing.create();
        hashing.update(name);
        hashing.update(level);
        hashing.update(exceptionClassName);
        if (stackTraceElements != null) {
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                hashing.update(stackTraceElement.getClassName());
                hashing.update(stackTraceElement.getLineNumber());
                hashing.update(stackTraceElement.getMethodName());
            }
        } else {
            hashing.update(message);
        }
        correlationId = hashing.asString();
        return correlationId;
    }

    public enum Level {

        ERROR(0),
        WARN(1),
        INFO(2),
        DEBUG(3),
        TRACE(4);

        private int value;

        Level(int value) {
            this.value = value;
        }

        public boolean isHigherSeverity(Level level) {
            requireNonNull(level);
            return value <= level.value;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Level.class.getSimpleName() + "[", "]")
                    .add("name=" + name())
                    .add("value=" + value)
                    .toString();
        }
    }
}
