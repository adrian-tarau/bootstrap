package net.microfalx.bootstrap.logger;

import lombok.*;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;

import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseName;

/**
 * An alert with counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlertEvent implements Identifiable<String>, Timestampable<LocalDateTime> {

    private String id;
    private LoggerEvent event;
    private long createdAt;
    private long updatedAt;

    private int totalEventCount;
    private int pendingEventCount;
    private boolean acknowledged;

    private String failureClass;
    private String failureType;

    public String getMessage() {
        return event.getMessage();
    }

    public LoggerEvent.Level getLevel() {
        return event.getLevel();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(createdAt);
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return TimeUtils.toLocalDateTime(updatedAt);
    }

    /**
     * Updates the alert with new information from a logger event.
     *
     * @param event the new event
     */
    public synchronized void update(LoggerEvent event) {
        requireNonNull(event);
        this.event = event;
        long timestamp = event.getTimestamp();
        if (createdAt == 0) {
            createdAt = timestamp;
            updatedAt = createdAt;
        } else {
            if (timestamp > updatedAt) updatedAt = timestamp;
        }
        failureClass = event.getExceptionClassName();
        failureType = StringUtils.isNotEmpty(failureClass) ? getRootCauseName(failureClass) : null;
        acknowledged = false;
        totalEventCount++;
        pendingEventCount++;
    }
}
