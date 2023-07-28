package net.microfalx.bootstrap.model;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class FilterImpl implements Filter {

    private final Expression expression;
    private final int offset;
    private final int limit;

    FilterImpl(Expression expression, int offset, int limit) {
        requireNonNull(expression);
        requireBounded(offset, 0, Integer.MAX_VALUE);
        requireBounded(limit, -1, Integer.MAX_VALUE);
        this.expression = expression;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public String getDescription() {
        return expression.getDescription();
    }

    @Override
    public String getName() {
        return expression.getName();
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public boolean isEmpty() {
        return expression instanceof LogicalExpression && ((LogicalExpression) expression).getExpressions().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterImpl filter)) return false;

        if (offset != filter.offset) return false;
        if (limit != filter.limit) return false;
        return expression.equals(filter.expression);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + offset;
        result = 31 * result + limit;
        return result;
    }

    @Override
    public String toString() {
        return "FilterImpl{" +
                "expression=" + expression +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
