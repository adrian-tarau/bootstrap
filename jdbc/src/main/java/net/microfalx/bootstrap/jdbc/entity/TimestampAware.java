package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public abstract class TimestampAware implements Serializable {

    @Serial
    private static final long serialVersionUID = 1541768280285586132L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was created")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "modified_at")
    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was last time modified")
    private LocalDateTime modifiedAt;

    @PrePersist
    public void updateTimestamps() {
        modifiedAt = LocalDateTime.now();
    }
}
