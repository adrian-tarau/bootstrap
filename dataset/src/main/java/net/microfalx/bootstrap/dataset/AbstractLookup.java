package net.microfalx.bootstrap.dataset;

import lombok.*;
import net.microfalx.lang.annotation.Id;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

/**
 * A base class for lookups.
 *
 * @param <T> the identifier type
 */
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class AbstractLookup<T> implements Lookup<T> {

    @Id
    private T id;

    @Name
    private String name;
}
