package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.Timestampable;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 */
@MappedSuperclass
@ToString(callSuper = true)
@Getter
@Setter
public abstract class TimestampAware implements Timestampable<LocalDateTime>, Serializable {

    @Serial
    private static final long serialVersionUID = 1541768280285586132L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the {name} was created")
    @Formattable(tooltip = EntityFormatters.CreatedAtTooltip.class)
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the {name} was last time modified")
    @Formattable(tooltip = EntityFormatters.ModifiedAtTooltip.class)
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime modifiedAt;

    @PrePersist
    public final void beforePersist() {
        updateTimestamps();
        updateOther();
    }

    @PreUpdate
    public final void beforeUpdate() {
        updateTimestamps();
        updateOther();
    }

    protected void updateTimestamps() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    protected void updateOther() {

    }
}
