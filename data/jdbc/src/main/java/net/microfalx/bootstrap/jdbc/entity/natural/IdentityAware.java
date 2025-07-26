package net.microfalx.bootstrap.jdbc.entity.natural;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A base class for all entities which can be identified (which is all) and the primary key is a natural key.
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public abstract class IdentityAware<T extends Serializable> implements Identifiable<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1023653519669708398L;

    @Id
    @Column(name = "id")
    @Position(1)
    @Visible(false)
    private T id;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(ClassUtils.isSubClassOf(o.getClass(), getClass()))) return false;
        return Objects.equals(id, ((IdentityAware<T>) o).id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
