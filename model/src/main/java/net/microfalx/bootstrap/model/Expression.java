package net.microfalx.bootstrap.model;

/**
 * An interface for a query expression.
 */
public interface Expression {

    /**
     * Returns the value of the expression.
     *
     * @return a non-null instance
     */
    String getValue();
}
