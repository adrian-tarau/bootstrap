package net.microfalx.bootstrap.model;

import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class LogicalExpressionImpl extends AbstractComparisonExpressionLocator implements LogicalExpression {

    private final List<Expression> expressions;
    private final Operator operator;

    LogicalExpressionImpl(Operator operator, Collection<Expression> expressions) {
        requireNonNull(operator);
        requireNonNull(expressions);
        this.expressions = new ArrayList<>(expressions);
        this.operator = operator;
        if (operator == Operator.NOT && expressions.size() > 1) {
            throw new ExpressionException("NOT operator expects zero or one sub-expression, got " + expressions);
        }
    }

    @Override
    public String getName() {
        return operator.name().toLowerCase() + "(expr=" + expressions.size() + ")";
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        if (expressions.size() == 1 && operator == Operator.NOT) {
            builder.append(operator.getLabel());
        }
        builder.append("(");
        Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            Expression expression = iterator.next();
            builder.append(expression.getDescription());
            if (iterator.hasNext()) builder.append(' ').append(operator.getLabel()).append(' ');
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public LogicalExpression operator(LogicalExpression.Operator operator) {
        requireNonNull(operator);
        return new LogicalExpressionImpl(operator, new ArrayList<>(expressions));
    }

    @Override
    public LogicalExpression append(Expression expression) {
        requireNonNull(expression);
        LogicalExpressionImpl copy = copy();
        copy.expressions.add(expression);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogicalExpressionImpl that = (LogicalExpressionImpl) o;
        return Objects.equals(expressions, that.expressions) && operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions, operator);
    }

    private LogicalExpressionImpl copy() {
        return new LogicalExpressionImpl(operator, new ArrayList<>(expressions));
    }

    @Override
    public String toString() {
        return "LogicalExpressionImpl{" +
                "expressions=" + expressions +
                ", operator=" + operator +
                '}';
    }

    @Override
    Expression getRootExpression() {
        return this;
    }
}
