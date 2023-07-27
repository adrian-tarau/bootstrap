package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ObjectUtils.toArray;

class ComparisonExpressionImpl implements ComparisonExpression {

    private final String field;
    private final Object value;
    private final Operator operator;

    ComparisonExpressionImpl(Operator operator, String field) {
        this(operator, field, null);
    }

    ComparisonExpressionImpl(Operator operator, String field, Object value) {
        requireNonNull(operator);
        requireNonNull(field);
        this.field = field;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String getName() {
        return field;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(field).append(' ').append(operator.getLabel()).append(' ');
        boolean shouldQuote = value instanceof String;
        if (shouldQuote) builder.append("'");
        builder.append(ObjectUtils.toString(value));
        if (shouldQuote) builder.append("'");
        return builder.toString();
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object[] getValues() {
        return toArray(value);
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparisonExpressionImpl that = (ComparisonExpressionImpl) o;
        return Objects.equals(field, that.field) && Objects.equals(value, that.value) && operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, value, operator);
    }

    @Override
    public String toString() {
        return "ComparisonExpressionImpl{" +
                "field='" + field + '\'' +
                ", value=" + value +
                ", operator=" + operator +
                '}';
    }
}
