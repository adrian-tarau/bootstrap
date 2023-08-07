package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;

/**
 * A base class for all entities which can be named and have timestamps.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public abstract class NamedTimestampAware extends TimestampAware {

    @Column(name = "name", nullable = false)
    @NotBlank
    @Name
    @Position(5)
    private String name;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    private String description;

}
