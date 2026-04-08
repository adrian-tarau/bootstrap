package net.microfalx.bootstrap.support.alert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.Formatters;
import net.microfalx.bootstrap.dataset.model.IdentityAware;
import net.microfalx.bootstrap.logger.AlertEvent;
import net.microfalx.bootstrap.logger.LoggerEvent;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Alerts")
@ReadOnly
public class Alert extends IdentityAware<String> {

    @Position(1)
    @Description("The time when the alert started to fire")
    @CreatedDate
    @CreatedAt
    @Formattable(tooltip = Formatters.CreatedAtTooltip.class, elapsed = true)
    private LocalDateTime createdAt;

    @Position(2)
    @Description("The last time when the alert fired")
    @OrderBy(OrderBy.Direction.DESC)
    @Formattable(tooltip = Formatters.ModifiedAtTooltip.class, elapsed = true)
    @LastModifiedDate
    @ModifiedAt
    @Timestamp
    private LocalDateTime modifiedAt;

    @Position(3)
    @Description("The severity level")
    private LoggerEvent.Level level;

    @Position(10)
    @Name
    @Description("The message associated with the alert")
    @Width("40%")
    private String message;

    @Position(11)
    @Label(value = "Failure Type")
    private String failureType;

    @Position(15)
    @Description("Indicates whether the alert has been acknowledged")
    private boolean acknowledged;

    @Position(20)
    @Label(value = "Total", group = "Counts")
    @Description("The total number of alerts received")
    private long totalCount;

    @Position(21)
    @Label(value = "Pending", group = "Counts")
    @Description("The total number of alerts received since the last acknowledgement")
    private long pendingCount;

    @Position(31)
    @Label(value = "Exception Class")
    @Visible(false)
    private String failureClass;

    public static Alert from(AlertEvent event) {
        if (event == null) return null;
        Alert model = new Alert();
        model.setId(event.getId());
        model.setCreatedAt(event.getCreatedAt());
        model.setModifiedAt(event.getModifiedAt());
        model.setLevel(event.getLevel());
        model.setMessage(event.getMessage());
        model.setAcknowledged(event.isAcknowledged());
        model.setFailureType(event.getFailureType());
        model.setFailureClass(event.getFailureClass());
        model.setTotalCount(event.getTotalEventCount());
        model.setPendingCount(event.getPendingEventCount());
        return model;
    }
}
