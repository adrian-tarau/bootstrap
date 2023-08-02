package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@MappedSuperclass
@ToString
public abstract class TimestampAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "modified_at")
    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    private LocalDateTime modifiedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @PrePersist
    public void updateTimestamps() {
        modifiedAt = LocalDateTime.now();
    }
}
