package net.microfalx.bootstrap.security.group;

import jakarta.persistence.*;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;

import java.util.Objects;

@Entity
@Table(name = "security_groups")
public class Group extends NamedTimestampAware {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group group)) return false;

        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
