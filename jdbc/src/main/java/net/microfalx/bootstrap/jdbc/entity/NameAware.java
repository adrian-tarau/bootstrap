package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import net.microfalx.bootstrap.dataset.annotation.Component;

/**
 * A base class for all entities which can be named and have an optional description.
 */
@MappedSuperclass
public abstract class NameAware {

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "description")
    @Component(Component.Type.TEXT_AREA)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NameAware{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
