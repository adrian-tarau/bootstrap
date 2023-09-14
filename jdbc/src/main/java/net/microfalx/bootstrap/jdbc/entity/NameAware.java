package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.lang.annotation.Position;

import java.io.Serial;
import java.io.Serializable;

/**
 * A base class for all entities which can be named and have an optional description.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public abstract class NameAware implements Serializable {

    @Serial
    private static final long serialVersionUID = -720697127573911840L;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Position(5)
    private String name;

    @Column(name = "description")
    @Component(Component.Type.TEXT_AREA)
    @Position(1000)
    private String description;

}
