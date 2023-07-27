package net.microfalx.bootstrap.model;

import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class LogicalExpressionImpl implements LogicalExpression {

    private final List<Expression> expressions;
    private final Operator operator;

    LogicalExpressionImpl(Operator operator, Collection<Expression> expressions) {
        requireNonNull(operator);
        requireNonNull(expressions);
        this.expressions = new ArrayList<>(expressions);
        this.operator = operator;
    }

    @Override
    public String getName() {
        return operator.name().toLowerCase() + "(expr=" + expressions.size() + ")";
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
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

    @Override
    public String toString() {
        return "LogicalExpressionImpl{" +
                "expressions=" + expressions +
                ", operator=" + operator +
                '}';
    }
}
