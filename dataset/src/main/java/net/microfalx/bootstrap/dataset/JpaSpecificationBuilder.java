package net.microfalx.bootstrap.dataset;

import jakarta.persistence.criteria.*;
import net.microfalx.bootstrap.model.Expression;
import net.microfalx.bootstrap.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which builds {@link org.springframework.data.jpa.domain.Specification} based on a {@link net.microfalx.bootstrap.model.Filter}.
 *
 * @param <M> the model type
 */
class JpaSpecificationBuilder<M, F extends Field<M>, ID> implements Specification<M> {

    private final DataSetService dataSetService;
    private final Metadata<M, F, ID> metadata;
    private final Filter filter;

    public JpaSpecificationBuilder(DataSetService dataSetService, Metadata<M, F, ID> metadata, Filter filter) {
        requireNonNull(dataSetService);
        requireNonNull(metadata);
        this.dataSetService = dataSetService;
        this.metadata = metadata;
        this.filter = filter;
    }

    public Specification<M> build() {
        return this;
    }

    @Override
    public Predicate toPredicate(Root<M> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return toPredicate(filter.getExpression(), root, criteriaBuilder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate toPredicate(Expression expression, Root<M> root, CriteriaBuilder criteriaBuilder) {
        if (expression instanceof LogicalExpression) {
            LogicalExpression logicalExpression = (LogicalExpression) expression;
            LogicalExpression.Operator operator = logicalExpression.getOperator();
            List<Expression> childExpressions = logicalExpression.getExpressions();
            if (childExpressions.size() == 1 && operator != LogicalExpression.Operator.NOT) {
                expression = childExpressions.iterator().next();
                if (expression instanceof ComparisonExpression) {
                    return toComparisonPredicate(expression, root, criteriaBuilder);
                } else {
                    return toPredicate(expression, root, criteriaBuilder);
                }
            } else {
                Collection<Predicate> predicates = new ArrayList<>();
                for (Expression childExpression : childExpressions) {
                    Predicate predicate = toPredicate(childExpression, root, criteriaBuilder);
                    if (predicate != null) predicates.add(predicate);
                }
                if (predicates.isEmpty()) return null;
                return switch (operator) {
                    case AND -> criteriaBuilder.and(predicates.toArray(Predicate[]::new));
                    case OR -> criteriaBuilder.or(predicates.toArray(Predicate[]::new));
                    case NOT -> criteriaBuilder.not(predicates.iterator().next());
                };
            }
        } else {
            return toComparisonPredicate(expression, root, criteriaBuilder);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Predicate toComparisonPredicate(Expression expression, Root<M> root, CriteriaBuilder criteriaBuilder) {
        ComparisonExpression comparisonExpression = (ComparisonExpression) expression;
        ComparisonExpression.Operator operator = comparisonExpression.getOperator();
        F field = metadata.get(comparisonExpression.getField());
        Path path = root.get(field.getProperty());
        if (operator == ComparisonExpression.Operator.BETWEEN) {
            Object valueMin = Field.from(comparisonExpression.getValues()[0], field.getDataClass());
            Object valueMax = Field.from(comparisonExpression.getValues()[1], field.getDataClass());
            return criteriaBuilder.between(path, (Comparable) valueMin, (Comparable) valueMax);
        } else {
            if (ComparisonExpression.MATCH_ALL.equals(comparisonExpression.getValue())) return null;
            Object value = dataSetService.resolve(field, comparisonExpression.getValue());
            return switch (operator) {
                case EQUAL -> criteriaBuilder.equal(path, value);
                case NOT_EQUAL -> criteriaBuilder.notEqual(path, value);
                case LESS -> criteriaBuilder.lessThan(path, (Comparable) value);
                case LESS_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(path, (Comparable) value);
                case GREATER -> criteriaBuilder.greaterThan(path, (Comparable) value);
                case GREATER_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) value);
                case NULL -> criteriaBuilder.isNull(path);
                case NOT_NULL -> criteriaBuilder.isNotNull(path);
                case LIKE -> criteriaBuilder.like(path, Field.from(value, String.class));
                case IN -> criteriaBuilder.in(path).value(value);
                case CONTAINS -> criteriaBuilder.like(path, "%" + Field.from(value, String.class) + "%");
                default -> throw new DataSetException("Unsupported comparison operator: " + operator);
            };
        }
    }

}
