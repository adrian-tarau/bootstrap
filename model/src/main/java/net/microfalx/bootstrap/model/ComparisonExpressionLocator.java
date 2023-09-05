package net.microfalx.bootstrap.model;

import java.util.List;

/**
 * An interface which allows clients to locate comparison expressions.
 */
public interface ComparisonExpressionLocator {

    /**
     * Finds first comparison expression for a given field.
     * <p>
     * If there are multiple with the same field, first one is returned regardless how many exist.
     *
     * @param field the field name
     * @return the expression, null if such expression does not exist
     */
    ComparisonExpression findExpression(String field);

    /**
     * Finds all comparison expression for a given field.
     * <p>
     * If there are multiple with the same field, first one is returned
     *
     * @param field the field name
     * @return a non-null instance
     */
    List<ComparisonExpression> findExpressions(String field);

    /**
     * Finds all comparison expressions.
     *
     * @return a non-null instance
     */
    List<ComparisonExpression> getComparisonExpressions();
}
