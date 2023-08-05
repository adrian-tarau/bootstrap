package net.microfalx.bootstrap.model;

import net.microfalx.lang.ObjectUtils;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ObjectUtils.isEmpty;

/**
 * A class which filters a list of models.
 *
 * @param <M> the model type
 */
public class ModelFilter<M> {

    private final Metadata<M, ? extends Field<M>, ?> metadata;
    private final List<M> models;
    private final Filter filter;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public ModelFilter(Metadata<M, ? extends Field<M>, ?> metadata, List<M> models, Filter filter) {
        requireNonNull(metadata);
        requireNonNull(models);
        this.metadata = metadata;
        this.models = new ArrayList<>(models);
        this.filter = filter != null ? filter : Filter.create();
        this.matcher.setCachePatterns(true);
        this.matcher.setCaseSensitive(false);
    }

    /**
     * Applies the filter to the models.
     *
     * @return the sorted models
     */
    public List<M> apply() {
        return models.stream().filter(m -> evaluateExpression(filter.getExpression(), m)).toList();
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
        Object fieldValue = Field.from(field.get(model), field.getDataClass());
        return switch (comparisonExpression.getOperator()) {
            case NOT_EQUAL -> !comparisonExpression.getValue().equals(fieldValue);
            case EQUAL -> comparisonExpression.getValue().equals(fieldValue);
            case REGEX -> regexMatch(ObjectUtils.toString(comparisonExpression.getValue()),
                    ObjectUtils.toString(fieldValue));
            case LIKE -> globMatch(ObjectUtils.toString(comparisonExpression.getValue()),
                    ObjectUtils.toString(fieldValue));
            case CONTAINS -> ObjectUtils.toString(fieldValue)
                    .contains(ObjectUtils.toString(comparisonExpression.getValue()));
            case BETWEEN -> evaluateBetweenExpression(comparisonExpression, fieldValue);
            case LESS -> toDouble(fieldValue) < toDouble(comparisonExpression.getValue());
            case LESS_OR_EQUAL -> toDouble(fieldValue) <= toDouble(comparisonExpression.getValue());
            case GREATER -> toDouble(fieldValue) > toDouble(comparisonExpression.getValue());
            case GREATER_OR_EQUAL -> toDouble(fieldValue) >= toDouble(comparisonExpression.getValue());
            case NOT_IN -> !isIn(comparisonExpression, fieldValue);
            case IN -> isIn(comparisonExpression, fieldValue);
            case NOT_NULL -> !isEmpty(fieldValue);
            case NULL -> isEmpty(fieldValue);
        };
    }

    private boolean evaluateBetweenExpression(ComparisonExpression comparisonExpression, Object fieldValue) {
        Object[] values = comparisonExpression.getValues();
        Field.from(values[0], Double.class);
        double numericValue = Field.from(fieldValue, Double.class);
        double min = Field.from(values[0], Double.class);
        double max = Field.from(values[1], Double.class);
        return numericValue >= min && numericValue <= max;
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
                "models=" + models.size() +
                ", filter=" + filter +
                '}';
    }
}
