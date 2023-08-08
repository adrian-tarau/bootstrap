package net.microfalx.bootstrap.dataset;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.annotation.Id;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A base class for lookups.
 *
 * @param <T> the identifier type
 */
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public abstract class AbstractLookup<T> implements Lookup<T> {

    @Id
    private T id;

    @Name
    private String name;

    public AbstractLookup(T id, String name) {
        requireNonNull(id);
        requireNonNull(name);
        this.id = id;
        this.name = name;
    }
}
