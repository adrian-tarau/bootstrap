package net.microfalx.bootstrap.model;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

import java.util.Collections;

/**
 * Abstraction filter information.
 */
public interface Filter extends Nameable, Descriptable, ComparisonExpressionLocator {

    /**
     * An empty filter.
     */
    Filter EMPTY = create();

    /**
     * A constant for no limit for pagination.
     */
    int NO_LIMIT = -1;

    /**
     * Creates an empty filter.
     *
     * @return a non-null instance
     */
    static Filter create() {
        return new FilterImpl(LogicalExpression.and(Collections.emptyList()), 0, Filter.NO_LIMIT);
    }

    /**
     * Creates a filter from an expression.
     *
     * @param expression the expression
     * @return a non-null instance
     */
    static Filter create(Expression expression) {
        return new FilterImpl(expression, 0, Filter.NO_LIMIT);
    }

    /**
     * Creates a filter from an expression and provides the page to select
     *
     * @param expression the expression
     * @param offset     the offset of the first model
     * @param limit      the maximum number of models to return (page size)
     * @return a non-null instance
     */
    static Filter create(Expression expression, int offset, int limit) {
        return new FilterImpl(expression, offset, limit);
    }

    /**
     * Returns the root expression of the filter.
     *
     * @return a non-null instance
     */
    Expression getExpression();

    /**
     * Returns the attributes associated with a filter.
     *
     * @return a collection of attributes
     */
    Attributes<Attribute> getAttributes();

    /**
     * Finds the comparison expression for a given field.
     *
     * @param fieldName the field name
     * @return the expression, null if none could be found
     */
    ComparisonExpression find(String fieldName);

    /**
     * Returns the index of the first model and it used to paginate
     *
     * @return a positive integer, 0 is first record
     */
    int getOffset();

    /**
     * Returns the number of models to return, starting from {@link #getOffset()} ()}
     *
     * @return a positive integer if pagination is requested, 1 if no pagination
     */
    int getLimit();

    /**
     * Returns whether the filter is empty.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns a hash which identifies uniquely a filter.
     *
     * @return a non-null instance
     */
    String getHash();
}
