package net.microfalx.bootstrap.dataset;

/**
 * A default lookup implementation with a given identifier type.
 *
 * @param <T> the lookup identifier
 */
public class DefaultLookup<T> extends AbstractLookup<T> {

    public DefaultLookup(T id, String name) {
        super(id, name);
    }
}
