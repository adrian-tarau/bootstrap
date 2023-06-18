package net.microfalx.bootstrap.model;

/**
 * Abstraction filter information.
 */
public interface Filter {

    /**
     * Returns the root expression of the filter.
     *
     * @return a non-null instance
     */
    Expression getExpression();

    /**
     * Returns whether the filter is empty.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns the string representation of the filter.
     *
     * @return a non-null instance
     */
    String getValue();
}
