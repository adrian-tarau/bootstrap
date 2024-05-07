package net.microfalx.bootstrap.model;

import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.StringUtils;

import java.util.*;

import static net.microfalx.bootstrap.model.ComparisonExpression.contains;
import static net.microfalx.bootstrap.model.ComparisonExpression.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * A parser for a query used to filter a data set.
 */
public final class QueryParser<M, F extends Field<M>, ID> {

    private final Metadata<M, F, ID> metadata;
    private final String text;

    private char[] buffer;
    private int position;
    private int previousPosition;
    private boolean quoted;
    private Queue<Expression> expressions = new ArrayDeque<>();
    private LogicalExpression.Operator operator = LogicalExpression.Operator.AND;
    private Set<String> defaultFields = new HashSet<>();

    public QueryParser(Metadata<M, F, ID> metadata, String text) {
        requireNonNull(metadata);
        this.metadata = metadata;
        this.text = emptyIfNull(text);
        this.buffer = this.text.toCharArray();
    }

    /**
     * Returns the expression behind the query.
     *
     * @return a non-null instance
     */
    public LogicalExpression parse() {
        doParse();
        return LogicalExpression.create(operator, expressions);
    }

    /**
     * Returns whether the expression is valid.
     * <p>
     * An expression is valid if the expression references fields form the data set.
     *
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isValid() {
        return validate() == null;
    }

    /**
     * Registers a default fields.
     * <p>
     * Default fields are used to turn text queries without operator and operands into <code>contain</code> expressions.
     *
     * @param fieldName the field name
     * @return self
     */
    public QueryParser<M, F, ID> addDefaultField(String fieldName) {
        requireNotEmpty(fieldName);
        this.defaultFields.add(fieldName);
        return this;
    }

    /**
     * Registers default fields.
     * <p>
     * Default fields are used to turn text queries without operator and operands into <code>contain</code> expressions.
     *
     * @param fieldNames the field names
     * @return self
     */
    public QueryParser<M, F, ID> addDefaultFields(Collection<String> fieldNames) {
        requireNonNull(fieldNames);
        this.defaultFields.addAll(fieldNames);
        return this;
    }

    /**
     * Validates and if the validation fails, it returns the reason why it fails.
     *
     * @return the error message if there are errors, null if successful
     */
    public String validate() {
        try {
            LogicalExpression parsedExpression = parse();
            for (Expression childExpression : parsedExpression.getExpressions()) {
                if (childExpression instanceof ComparisonExpression) {
                    String fieldName = ((ComparisonExpression) childExpression).getField();
                    if (metadata.find(fieldName) == null) return "A field with name '" + fieldName + "' does not exist";
                }
            }
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void doParse() {
        reset();
        int maxIterations = 20;
        while (hasNext() && maxIterations-- > 0) {
            Expression expression = parseExpression();
            skipWhitespaces();
            expressions.offer(expression);
            if (hasNext() && isLogicalOperator(peek(TokenType.IDENTIFIER))) {
                operator = EnumUtils.fromName(LogicalExpression.Operator.class, next(TokenType.IDENTIFIER), LogicalExpression.Operator.AND);
            }
        }
        if (maxIterations == 0) {
            throw new QueryException("Infinite loop while parsing '" + text + "'");
        }
    }

    private Expression parseExpression() {
        String attributeName = next(TokenType.IDENTIFIER);
        String attributeOperator = peek(TokenType.SYMBOLS, 2);
        if ((isEmpty(attributeOperator) || isLogicalOperator(attributeOperator)) && !defaultFields.isEmpty()) {
            Collection<Expression> expressionCollection = new ArrayList<>();
            for (String defaultField : defaultFields) {
                expressionCollection.add(ComparisonExpression.contains(defaultField, attributeName));
            }
            return LogicalExpression.or(expressionCollection);
        } else if (isEmpty(attributeOperator)) {
            return raiseException("An operator is expected");
        }
        attributeOperator = next(TokenType.SYMBOLS, 2);
        ComparisonExpression.Operator operator = getComparisonOperator(attributeOperator);
        if (!has(TokenType.LITERAL)) raiseException("An attribute value is expected");
        String attributeValue = next(TokenType.LITERAL).trim();
        Object attributeValueRaw;
        char firstChar = attributeValue.charAt(0);
        if (isQuote(firstChar)) {
            attributeValueRaw = attributeValue = attributeValue.substring(1, attributeValue.length() - 1).trim();
        } else {
            attributeValueRaw = attributeValue;
        }
        switch (operator) {
            case EQUAL:
                if (attributeValue.length() > 0 && attributeValue.charAt(0) == OPEN_PARENTHESES_CHAR) {
                    String[] values = splitValues(attributeValue);
                    return in(attributeName, values);
                } else {
                    return eq(attributeName, attributeValueRaw);
                }
            case LIKE:
                return like(attributeName, attributeValueRaw);
            case CONTAINS:
                return contains(attributeName, attributeValueRaw);
            case NOT_EQUAL:
                return ne(attributeName, attributeValueRaw);
            case LESS:
                return lt(attributeName, attributeValueRaw);
            case LESS_OR_EQUAL:
                return le(attributeName, attributeValueRaw);
            case GREATER:
                return gt(attributeName, attributeValueRaw);
            case GREATER_OR_EQUAL:
                return ge(attributeName, attributeValueRaw);
            default:
                return raiseException("Unknown operator '" + attributeOperator + "'", position);
        }
    }

    private ComparisonExpression.Operator getComparisonOperator(String value) {
        return switch (value) {
            case "=", "==" -> Operator.EQUAL;
            case "~" -> Operator.CONTAINS;
            case "!=" -> Operator.NOT_EQUAL;
            case "<" -> Operator.LESS;
            case "<=" -> Operator.LESS_OR_EQUAL;
            case ">" -> Operator.GREATER;
            case ">=" -> Operator.GREATER_OR_EQUAL;
            default -> raiseException("Unknown operator '" + value + "'", position);
        };
    }

    private void skip(TokenType type) {
        next(type);
    }

    private String[] splitValues(String values) {
        values = values.substring(1, values.length() - 1).trim();
        return StringUtils.split(values, MULTIPLE_VALUES_SEPARATOR);
    }

    private String peek(TokenType type) {
        int currentPosition = position;
        String token = next(type);
        position = currentPosition;
        return token;
    }

    private String peek(TokenType type, int maximumTokens) {
        int currentPosition = position;
        String token = next(type, maximumTokens);
        position = currentPosition;
        return token;
    }

    private String next(TokenType type) {
        return next(type, type == TokenType.SYMBOL ? 1 : Integer.MAX_VALUE);
    }

    private String next(TokenType type, int maximumTokens) {
        quoted = false;
        skipWhitespaces();
        previousPosition = position;
        StringBuilder token = new StringBuilder();
        int tokenCount = 0;
        while (has(type)) {
            char c = pollChar();
            token.append(c);
            if (++tokenCount == maximumTokens) break;
            if (type == TokenType.LITERAL && quoted && isQuote(c) && tokenCount > 1) break;
        }
        skipWhitespaces();
        return token.toString();
    }

    private char pollChar() {
        return buffer[position++];
    }

    private char peekChar() {
        return buffer[position];
    }

    private boolean has(TokenType type) {
        if (!hasNext()) return false;
        char c = peekChar();
        switch (type) {
            case ANY:
                return isAny(c) && !isSymbol(c);
            case LITERAL:
                if (!quoted && isQuote(c)) {
                    quoted = true;
                    return true;
                } else {
                    return isLiteral(c, quoted);
                }
            case IDENTIFIER:
                return isIdentifier(c);
            case SYMBOL:
            case SYMBOLS:
                return isSymbol(c);
            default:
                throw new IllegalStateException("Unknown token type " + type);
        }
    }

    private <T> T raiseException(String message) {
        return raiseException(message, position);
    }

    private <T> T raiseException(String message, int position) {
        message += " at position " + (position + 1);
        message += " in expression '" + new String(buffer) + "'";
        throw new QueryException(message);
    }

    private boolean isSymbol(char c) {
        return containsInArray(c, SYMBOLS);
    }

    private boolean isLiteral(char c, boolean quoted) {
        boolean literal = isIdentifier(c) || containsInArray(c, LITERAL_SYMBOLS);
        return literal || (quoted && isWhitespace(c)) || isQuote(c);
    }

    private boolean isQuote(char c) {
        return containsInArray(c, QUOTES);
    }

    private boolean isAny(char c) {
        return !isWhitespace(c);
    }

    private boolean isIdentifier(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || containsInArray(c, IDENTIFIER);
    }

    private boolean isLogicalOperator(String value) {
        return containsInArray(value, LOGICAL_OPERATORS);
    }

    private boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    private void skipWhitespaces() {
        while (hasNext() && isWhitespace(peekChar())) {
            position++;
        }
    }

    private void reset() {
        position = 0;
        previousPosition = 0;
        quoted = false;
        expressions.clear();
        operator = LogicalExpression.Operator.AND;
    }

    private boolean hasNext() {
        return position < buffer.length;
    }

    enum TokenType {
        ANY,
        LITERAL,
        IDENTIFIER,
        SYMBOL,
        SYMBOLS
    }

    private static final char[] SYMBOLS = new char[]{'(', ')', '{', '}', '=', '!', '<', '>', ',', '~'};
    private static final char[] LITERAL_SYMBOLS = new char[]{'`', '~', '!', '@', '#', '$', '%', '^', '&', '*', '_', '-', '(', ')', '=', '+',
            ':', ';', '?', '.', '|', '<', '>'};
    private static final char[] QUOTES = new char[]{'\'', '\"', '`'};
    private static final char[] IDENTIFIER = new char[]{'_', '-'};
    private static final String[] LOGICAL_OPERATORS = new String[]{"and", "or", "not"};

    private static final char OPEN_PARENTHESES_CHAR = '(';
    private static final String OPEN_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";

    private static final String OPEN_CURLY_BRACKET = "{";
    private static final String CLOSE_CURLY_BRACKET = "}";
    private static final String COMMA = ",";
    private static final String MULTIPLE_VALUES_SEPARATOR = "|";
}
