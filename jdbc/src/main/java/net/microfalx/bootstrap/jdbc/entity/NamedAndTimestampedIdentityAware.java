package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.Timestampable;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A base class for all entities which can be named and have timestamps.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public abstract class NamedAndTimestampedIdentityAware<T extends Serializable> extends NamedIdentityAware<T> implements Timestampable<LocalDateTime> {

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the {name} was created")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the {name} was last time modified")
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime modifiedAt;

    @PrePersist
    void beforePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (modifiedAt == null) modifiedAt = createdAt;
    }
}
