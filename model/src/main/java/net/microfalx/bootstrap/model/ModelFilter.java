package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TimeUtils;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.util.AntPathMatcher;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which filters a list of models.
 *
 * @param <M> the model type
 */
public class ModelFilter<M> {

    private final Metadata<M, ? extends Field<M>, ?> metadata;
    private final Iterable<M> models;
    private final Filter filter;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public ModelFilter(Metadata<M, ? extends Field<M>, ?> metadata, Iterable<M> models, Filter filter) {
        requireNonNull(metadata);
        requireNonNull(models);
        this.metadata = metadata;
        this.models = models;
        this.filter = filter != null ? filter : Filter.create();
        this.matcher.setCachePatterns(true);
        this.matcher.setCaseSensitive(false);
    }

    /**
     * Applies the filter to the models and returns the filtered models as a list.
     *
     * @return the filtered models
     */
    public List<M> toList() {
        return toStream().toList();
    }

    /**
     * Applies the filter to the models and returns the filtered models as a stream.
     *
     * @return the filtered models
     */
    public Stream<M> toStream() {
        Stream<M> stream = Streams.of(models);
        return stream.filter(m -> evaluateExpression(filter.getExpression(), m));
    }

    /**
     * Evaluates the expression recursively and returns whether the model's fields matches the expression.
     *
     * @param expression the expression
     * @param model      the model
     * @return {@code true} if all expressions are matching (record is included in the result),
     * {@code false} otherwise (record is excluded, filtered out)
     */
    private boolean evaluateExpression(Expression expression, M model) {
        if (expression instanceof ComparisonExpression) {
            return evaluateComparisonExpression(model, (ComparisonExpression) expression);
        } else if (expression instanceof LogicalExpression) {
            LogicalExpression logicalExpression = (LogicalExpression) expression;
            boolean matches = logicalExpression.getOperator() == LogicalExpression.Operator.AND;
            for (Expression subExpression : logicalExpression.getExpressions()) {
                boolean subMatch = evaluateExpression(subExpression, model);
                matches = switch (logicalExpression.getOperator()) {
                    case OR -> matches || subMatch;
                    case AND -> matches && subMatch;
                    case NOT -> !subMatch;
                };
            }
            return matches;
        }
        return false;
    }

    private boolean evaluateComparisonExpression(M model, ComparisonExpression comparisonExpression) {
        Field<M> field = metadata.get(comparisonExpression.getField());
        Object fieldValue = field.get(model);
        Object expressionValue = comparisonExpression.getValue();
        if (ComparisonExpression.MATCH_ALL.equals(expressionValue)) return true;
        ComparisonExpression.Operator operator = comparisonExpression.getOperator();
        if (operator == ComparisonExpression.Operator.BETWEEN) {
            return evaluateBetweenExpression(field, comparisonExpression, fieldValue);
        }
        if (operator == ComparisonExpression.Operator.NULL) {
            return fieldValue == null;
        }
        if (operator == ComparisonExpression.Operator.NOT_NULL) {
            return fieldValue != null;
        } else {
            if (expressionValue == null && fieldValue == null) return true;
            if (expressionValue == null || fieldValue == null) return false;
            expressionValue = Field.from(expressionValue, field.getDataClass());
            switch (operator) {
                case NOT_EQUAL:
                    return !expressionValue.equals(fieldValue);
                case EQUAL:
                    return expressionValue.equals(fieldValue);
                case REGEX:
                    return regexMatch(ObjectUtils.toString(expressionValue),
                            ObjectUtils.toString(fieldValue));
                case LIKE:
                    return globMatch(ObjectUtils.toString(expressionValue),
                            ObjectUtils.toString(fieldValue));
                case CONTAINS:
                    return StringUtils.contains(ObjectUtils.toString(fieldValue),
                            ObjectUtils.toString(expressionValue));
                case LESS:
                    return toDouble(fieldValue) < toDouble(comparisonExpression.getValue());
                case LESS_OR_EQUAL:
                    return toDouble(fieldValue) <= toDouble(comparisonExpression.getValue());
                case GREATER:
                    return toDouble(fieldValue) > toDouble(comparisonExpression.getValue());
                case GREATER_OR_EQUAL:
                    return toDouble(fieldValue) >= toDouble(comparisonExpression.getValue());
                case NOT_IN:
                    return !isIn(comparisonExpression, fieldValue);
                case IN:
                    return isIn(comparisonExpression, fieldValue);
                default:
                    throw new ModelException("Unhandled operator: " + operator);
            }
        }
    }

    private boolean evaluateBetweenExpression(Field<M> field, ComparisonExpression comparisonExpression, Object fieldValue) {
        Object[] values = comparisonExpression.getValues();
        if (field.getDataType().isTemporal()) {
            Temporal temporalValue = Field.from(fieldValue, Temporal.class);
            Temporal min = Field.from(values[0], Temporal.class);
            Temporal max = Field.from(values[1], Temporal.class);
            return compare(temporalValue, min) >= 0 && compare(temporalValue, max) <= 0;
        } else {
            double numericValue = Field.from(fieldValue, Double.class);
            double min = Field.from(values[0], Double.class);
            double max = Field.from(values[1], Double.class);
            return numericValue >= min && numericValue <= max;
        }
    }

    private int compare(Temporal temporal1, Temporal temporal2) {
        ZonedDateTime zonedDateTime1 = TimeUtils.toZonedDateTime(temporal1);
        ZonedDateTime zonedDateTime2 = TimeUtils.toZonedDateTime(temporal2);
        if (temporal1 == null) {
            return -1;
        } else if (temporal2 == null) {
            return 1;
        } else {
            return zonedDateTime1.compareTo(zonedDateTime2);
        }
    }

    private boolean isIn(ComparisonExpression comparisonExpression, Object fieldValue) {
        return stream(comparisonExpression.getValues()).anyMatch(o -> ObjectUtils.equals(o, fieldValue));
    }

    private boolean regexMatch(String pattern, String value) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                .matcher(value).matches();
    }

    private boolean globMatch(String pattern, String value) {
        return matcher.match(pattern, value);
    }

    private double toDouble(Object value) {
        if (value == null) return 0;
        return Field.from(value, Double.class);
    }

    @Override
    public String toString() {
        return "ModelFilter{" +
                "models=" + models +
                ", filter=" + filter +
                '}';
    }
}
