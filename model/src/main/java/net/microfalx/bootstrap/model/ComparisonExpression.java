package net.microfalx.bootstrap.model;

import net.microfalx.lang.ArgumentUtils;

import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * An expression which provides comparison for a single field.
 * <p>
 * For example for an EQUAL operation, FIELD = VALUE.
 */
public interface ComparisonExpression extends Expression {

    /**
     * A constant for an expression value which matches any value; such expressions are skipped when they are evaluated
     * in memory or they are skipped when converted to other expressions.
     */
    String MATCH_ALL = "*";

    /**
     * Creates a new comparison expression with an '=' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression eq(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.EQUAL, field, value);
    }

    /**
     * Creates a new comparison expression with an '<>' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression ne(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.NOT_EQUAL, field, value);
    }

    /**
     * Creates a new comparison expression with an '<' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression lt(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.LESS, field, value);
    }

    /**
     * Creates a new comparison expression with an '<=' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression le(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.LESS_OR_EQUAL, field, value);
    }

    /**
     * Creates a new comparison expression with an '>' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression gt(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.GREATER, field, value);
    }

    /**
     * Creates a new comparison expression with an '>' operator.
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression ge(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.GREATER_OR_EQUAL, field, value);
    }

    /**
     * Creates a new comparison expression with a 'between' operator.
     *
     * @param field the expression field
     * @param low   the low bound
     * @param high  the high bound
     * @return a non-null instance
     */
    static ComparisonExpression between(String field, Object low, Object high) {
        return new ComparisonExpressionImpl(Operator.BETWEEN, field, new Object[]{low, high});
    }

    /**
     * Creates a new comparison expression with an 'contains' operator (similar to like '*value*').
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression contains(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.CONTAINS, field, value);
    }

    /**
     * Creates a new comparison expression with an 'like' operator. Ex. FIELD_NAME like '*value*',
     * where * matches any number of characters and ? matches any single character
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression like(String field, Object value) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.LIKE, field, value);
    }

    /**
     * Creates a new comparison expression with an 'regex' operator.
     * For example:FIELD_NAME regex 'MATCHER', where MATCHER is a regular expression to be matches
     *
     * @param field the expression field
     * @param value the expression value
     * @return a non-null instance
     */
    static ComparisonExpression regex(String field, Object value) {
        return new ComparisonExpressionImpl(Operator.REGEX, field, value);
    }

    /**
     * Creates a new comparison expression with an 'in' operator. Ex. FIELD_NAME in (?,?,?).
     *
     * @param field  the expression field
     * @param values the expression value
     * @return a non-null instance
     */
    static ComparisonExpression in(String field, Collection<?> values) {
        ArgumentUtils.requireNonNull(values);
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.IN, field, values.toArray());
    }

    /**
     * Creates a new comparison expression with an 'in' operator. Ex. FIELD_NAME in (?,?,?).
     *
     * @param field  the expression field
     * @param values the expression value
     * @return a non-null instance
     */
    static ComparisonExpression in(String field, Object... values) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.IN, field, values);
    }

    /**
     * Creates a new comparison expression with an 'not in' operator. Ex. FIELD_NAME not in (?,?,?).
     *
     * @param field  the expression field
     * @param values the expression value
     * @return a non-null instance
     */
    static ComparisonExpression notIn(String field, Object... values) {
        return new ComparisonExpressionImpl(Operator.NOT_IN, field, values);
    }

    /**
     * Creates a new comparison expression with an 'is null' operator.
     *
     * @param field the expression field
     * @return a non-null instance
     */
    static ComparisonExpression isNull(String field) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.NULL, field);
    }

    /**
     * Creates a new comparison expression with an 'is not null' operator.
     *
     * @param field the expression field
     * @return a non-null instance
     */
    static ComparisonExpression isNotNull(String field) {
        return new ComparisonExpressionImpl(ComparisonExpression.Operator.NOT_NULL, field);
    }

    /**
     * Returns the field name used in the expression.
     *
     * @return a non-empty String
     */
    String getField();

    /**
     * Returns the value associated with the field name
     *
     * @return the field value
     */
    Object getValue();

    /**
     * Returns the values associated with the field name
     *
     * @return the field values
     */
    Object[] getValues();

    /**
     * Returns the operator of the expression.
     *
     * @return a non-null enum
     */
    Operator getOperator();

    /**
     * An enum for an operator
     */
    enum Operator {

        /**
         * Equal operator; FIELD_NAME = VALUE
         */
        EQUAL("=", 1),

        /**
         * Equal operator; FIELD_NAME <> VALUE
         */
        NOT_EQUAL("<>", 1),

        /**
         * Like operator; FIELD_NAME like '*va?ue*', where * matches any number of characters and ? matches any single character
         */
        LIKE("like", 1),

        /**
         * Regular expression operator; FIELD_NAME regex 'MATCHER', where MATCHER is a regular expression to be matches
         */
        REGEX("regex", 1),

        /**
         * Contains operator: FIELD_NAME contains 'value' or FIELD_NAME like '%value%'
         */
        CONTAINS("contains", 1),

        /**
         * Between operator; MIN < FIELD_NAME < MAX
         */
        BETWEEN("between", 2),

        /**
         * Less/smaller operator; FIELD_NAME < VALUE
         */
        LESS("<", 1),

        /**
         * Less or equal/smaller or equal operator; FIELD_NAME <= VALUE
         */
        LESS_OR_EQUAL("<=", 1),

        /**
         * Greater/bigger operator; FIELD_NAME > VALUE
         */
        GREATER(">", 1),

        /**
         * Greater or equal/bigger or equal operator; FIELD_NAME >= VALUE
         */
        GREATER_OR_EQUAL(">=", 1),

        /**
         * Not in operator; FIELD_NAME not in (...)
         */
        NOT_IN("not in", 1, Integer.MAX_VALUE),

        /**
         * In operator; FIELD_NAME not in (...)
         */
        IN("in", 1, Integer.MAX_VALUE),

        /**
         * Not null operator; FIELD_NAME is not null
         */
        NOT_NULL("not null", 0),

        /**
         * Null operator; FIELD_NAME is null
         */
        NULL("is null", 0);

        private final int minimumOperands;

        private final int maximumOperands;

        private final String label;

        Operator(String label, int operands) {
            this(label, operands, operands);
        }

        Operator(String label, int minimumOperands, int maximumOperands) {
            requireNotEmpty(label);
            this.label = label;
            this.minimumOperands = minimumOperands;
            this.maximumOperands = maximumOperands;
        }

        public int getMinimumOperands() {
            return minimumOperands;
        }

        public int getMaximumOperands() {
            return maximumOperands;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Operator{");
            sb.append("minimumOperands=").append(minimumOperands);
            sb.append(", maximumOperands=").append(maximumOperands);
            sb.append(", label='").append(label).append('\'');
            sb.append(", name=").append(name());
            sb.append('}');
            return sb.toString();
        }
    }
}
