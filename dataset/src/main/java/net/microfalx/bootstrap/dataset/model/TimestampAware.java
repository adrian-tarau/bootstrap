package net.microfalx.bootstrap.dataset.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.Timestampable;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A base class for all models which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@ToString
@Getter
@Setter
public abstract class TimestampAware implements Timestampable<LocalDateTime>, Serializable {

    @Serial
    private static final long serialVersionUID = 1541768280285586132L;

    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was created")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime createdAt;

    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was last time modified")
    @Timestamp
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime modifiedAt;
}
