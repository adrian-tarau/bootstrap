package net.microfalx.bootstrap.dataset.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.Timestampable;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * A base class for all models which can be named and have timestamps.
 */
@Getter
@Setter
@ToString(callSuper = true)
public abstract class NamedAndTimestampedIdentityAware<T> extends NamedIdentityAware<T> implements Timestampable<LocalDateTime> {

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
