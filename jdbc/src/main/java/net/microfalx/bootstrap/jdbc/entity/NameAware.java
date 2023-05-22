package net.microfalx.bootstrap.jdbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

/**
 * A base class for all entities which can be named.
 */
@MappedSuperclass
public abstract class NameAware {

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NameAware{" +
                "name='" + name + '\'' +
                '}';
    }
}
