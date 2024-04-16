package net.microfalx.bootstrap.web.controller.support.alert;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.logger.AlertEvent;
import net.microfalx.bootstrap.logger.LoggerEvent;
import net.microfalx.lang.annotation.*;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Name("Alerts")
@ReadOnly
public class Alert {

    @Id
    @Visible(value = false)
    private String id;

    @Position(1)
    @Description("The time when the alert started to fire")
    private LocalDateTime createdAt;

    @Position(2)
    @Description("The last time when the alert fired")
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime updatedAt;

    @Position(3)
    @Description("The severity level")
    private LoggerEvent.Level level;

    @Position(10)
    @Name
    @Description("The message associated with the alert")
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
        model.setUpdatedAt(event.getUpdatedAt());
        model.setLevel(event.getLevel());
        model.setMessage(StringUtils.abbreviate(event.getMessage(), 80));
        model.setAcknowledged(event.isAcknowledged());
        model.setFailureType(event.getFailureType());
        model.setFailureClass(event.getFailureClass());
        model.setTotalCount(event.getTotalEventCount());
        model.setPendingCount(event.getPendingEventCount());
        return model;
    }
}
