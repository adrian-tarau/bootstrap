package net.microfalx.bootstrap.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

public interface LogicalExpression extends Expression {

    static Expression[] EMPTY = new Expression[0];

    /**
     * Creates a new logical expression with an 'and' operator.
     *
     * @param expressions the expressions
     * @return a non-null instance
     */
    static LogicalExpression and(Expression... expressions) {
        return create(LogicalExpression.Operator.AND, expressions);
    }

    /**
     * Creates a new logical expression with an 'and' operator.
     *
     * @param expressions the expressions
     * @return a non-null instance
     */
    static LogicalExpression and(Collection<Expression> expressions) {
        return create(Operator.AND, expressions);
    }

    /**
     * Creates a new logical expression with an 'or' operator.
     *
     * @param expressions the expressions
     * @return a non-null instance
     */
    static LogicalExpression or(Expression... expressions) {
        return create(LogicalExpression.Operator.OR, expressions);
    }

    /**
     * Creates a new logical expression with an 'or' operator.
     *
     * @param expressions the expressions
     * @return a non-null instance
     */
    static LogicalExpression or(Collection<Expression> expressions) {
        return create(Operator.OR, expressions);
    }

    /**
     * Creates a new logical expression with an 'and' operator.
     *
     * @param expression the expression to negate
     * @return a non-null instance
     */
    static LogicalExpression not(Expression expression) {
        return new LogicalExpressionImpl(LogicalExpression.Operator.NOT, singletonList(expression));
    }

    /**
     * Creates a new logical (AND, OR) expression.
     *
     * @param operator    the operator between sub-expressions
     * @param expressions the sub-expressions
     * @return a new logical expression
     */
    static LogicalExpression create(LogicalExpression.Operator operator, Expression... expressions) {
        return new LogicalExpressionImpl(operator, Arrays.asList(expressions));
    }

    /**
     * Creates a new logical (AND, OR) expression.
     *
     * @param operator    the operator between sub-expressions
     * @param expressions the sub-expressions
     * @return a new logical expression
     */
    static LogicalExpression create(LogicalExpression.Operator operator, Collection<Expression> expressions) {
        return new LogicalExpressionImpl(operator, expressions);
    }

    /**
     * Returns the sub-expressions
     *
     * @return a list of sub-expressions or an empty list
     */
    List<Expression> getExpressions();

    /**
     * Returns the operator applied between expression.
     *
     * @return a non-null enum
     */
    Operator getOperator();

    /**
     * An enum for an operator
     */
    enum Operator {

        /**
         * Logical AND operator; expression1 AND expression2
         */
        AND("&&"),

        /**
         * Logical OR operator; expression1 OR expression2
         */
        OR("||"),

        /**
         * Logical NOT operator; not expression1
         */
        NOT("!");

        private final String label;

        Operator(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
