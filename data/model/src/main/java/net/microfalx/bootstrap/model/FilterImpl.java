package net.microfalx.bootstrap.model;

import net.microfalx.lang.Hashing;

import static net.microfalx.lang.ArgumentUtils.*;

class FilterImpl extends AbstractComparisonExpressionLocator implements Filter {

    private final Expression expression;
    private final int offset;
    private final int limit;
    private final Attributes<Attribute> attributes = Attributes.create();
    private String hash;

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
    public Expression getRootExpression() {
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
    public String getHash() {
        if (hash == null) {
            Hashing hashing = Hashing.create();
            updateHash(hashing, expression);
            hashing.update(offset);
            hashing.update(limit);
            return hash;
        }
        return hash;
    }

    @Override
    public Attributes<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public ComparisonExpression find(String fieldName) {
        requireNotEmpty(fieldName);
        return find(expression, fieldName);
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

    @Override
    public Expression getExpression() {
        return expression;
    }

    private void updateHash(Hashing hashing, Expression expression) {
        if (expression instanceof LogicalExpression logicalExpression) {
            hashing.update(logicalExpression.getOperator());
            for (Expression logicalExpressionExpression : logicalExpression.getExpressions()) {
                updateHash(hashing, logicalExpressionExpression);
            }
        } else {
            ComparisonExpression comparisonExpression = (ComparisonExpression) expression;
            hashing.update(comparisonExpression.getField());
            hashing.update(comparisonExpression.getOperator());
            hashing.update(comparisonExpression.getValue());
        }
    }

    private ComparisonExpression find(Expression expression, String fieldName) {
        if (expression instanceof LogicalExpression logicalExpression) {
            for (Expression logicalExpressionExpression : logicalExpression.getExpressions()) {
                ComparisonExpression comparisonExpression = find(logicalExpressionExpression, fieldName);
                if (comparisonExpression != null) return comparisonExpression;
            }
        } else {
            ComparisonExpression comparisonExpression = (ComparisonExpression) expression;
            if (comparisonExpression.getField().equalsIgnoreCase(fieldName)) return comparisonExpression;
        }
        return null;
    }
}
