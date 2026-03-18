package net.microfalx.bootstrap.dataset.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.annotation.Id;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A base class for all models which can be identified.
 */
@Getter
@Setter
@ToString
public abstract class IdentityAware<T> implements Identifiable<T>, Serializable {

    @Serial
    private static final long serialVersionUID = -5021428958396316891L;

    @Id
    @Position(1)
    @Visible(value = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T id;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityAware<?> instance)) return false;
        return Objects.equals(id, instance.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
