package net.microfalx.bootstrap.model;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Base class for all {@link ComparisonExpressionLocator}
 */
public abstract class AbstractComparisonExpressionLocator implements ComparisonExpressionLocator {

    /**
     * Returns the root expression for the locator.
     *
     * @return a non-null instance
     */
    abstract Expression getRootExpression();

    @Override
    public ComparisonExpression findExpression(String field) {
        List<ComparisonExpression> expressions = findExpressions(field);
        return expressions.isEmpty() ? null : expressions.iterator().next();
    }

    @Override
    public List<ComparisonExpression> findExpressions(String field) {
        requireNotEmpty(field);
        List<ComparisonExpression> expressionsByField = new ArrayList<>();
        collectExpressions(getRootExpression(), field, expressionsByField);
        return expressionsByField;
    }

    @Override
    public List<ComparisonExpression> getComparisonExpressions() {
        List<ComparisonExpression> expressionsByField = new ArrayList<>();
        collectExpressions(getRootExpression(), null, expressionsByField);
        return expressionsByField;
    }

    private void collectExpressions(Expression expression, String field, List<ComparisonExpression> expressions) {
        if (expression instanceof LogicalExpression) {
            LogicalExpression logicalExpression = (LogicalExpression) expression;
            for (Expression childExpression : logicalExpression.getExpressions()) {
                collectExpressions(childExpression, field, expressions);
            }
        } else {
            ComparisonExpression comparisonExpression = (ComparisonExpression) expression;
            if (field == null) {
                expressions.add(comparisonExpression);
            } else if (field.equalsIgnoreCase(comparisonExpression.getField())) {
                expressions.add(comparisonExpression);
            }
        }
    }
}
