package net.microfalx.bootstrap.dataset;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

/**
 * A model for lookups.
 * <p>
 * A lookup is a simple model made out of an identifier and a name (display value). If the lookup
 * implements {@link net.microfalx.lang.Descriptable}, a description field is available too.
 *
 * @param <T> the identifier type
 */
public interface Lookup<T> extends Identifiable<T>, Nameable {

}
