package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.support.*;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import java.util.Arrays;
import java.util.Stack;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Getter
@ToString
public final class Condition implements Nameable {

    private final String value;
    private final Migration migration;

    Condition(Migration migration, String value) {
        requireNonNull(migration);
        this.migration = migration;
        this.value = value;
    }

    public Migration getMigration() {
        return migration;
    }

    @Override
    public String getName() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public boolean evaluate(Schema schema) {
        if (StringUtils.isEmpty(value)) return true;
        Stack<String> conditionStack = new Stack<>();
        conditionStack.addAll(Arrays.asList(StringUtils.split(value, StringUtils.SPACE, true)));
        String objectType = conditionStack.pop();
        String name = conditionStack.pop();
        SchemaObject<?> schemaObject = getSchemaObject(schema, objectType, name);
        Operator operator = getOperator(conditionStack);
        Expression expression = new Expression(schemaObject, operator);
        return expression.evaluate();
    }

    private SchemaObject<?> getSchemaObject(Schema schema, String objectType, String name) {
        return switch (objectType.toLowerCase()) {
            case "table" -> schema.getTable(name);
            case "column" -> {
                String[] parts = StringUtils.split(value, ".", true);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid column specification, requires TABLE_NAME.COLUMN_NAME, got:  " + value);
                }
                Table<?> table = schema.getTable(parts[0]);
                Column<?> column = table.findColumn(parts[1]);
                yield column != null ? column : new ColumnImpl(table, parts[1]);
            }
            case "view" -> schema.getView(name);
            case "index" -> schema.getIndex(name);
            default -> throw new IllegalArgumentException("Unknown schema object type: " + objectType);
        };
    }

    private Operator getOperator(Stack<String> stack) {
        String token = stack.pop();
        boolean not = token.equals("not");
        if (not) token = stack.pop();
        return switch (token) {
            case "exists" -> {
                if (not) {
                    yield Operator.NOT_EXISTS;
                } else {
                    yield Operator.EXISTS;
                }
            }
            default -> throw new IllegalArgumentException("Unknown operator: " + token);
        };
    }

    enum Operator {
        EXISTS,
        NOT_EXISTS
    }

    static class ColumnImpl extends AbstractColumn<ColumnImpl> {

        public ColumnImpl(Table<?> table, String name) {
            super(table, name);
        }

        @Override
        public boolean exists() {
            return false;
        }
    }

    static class Expression {

        private final SchemaObject<?> schemaObject;
        private final Operator operator;

        Expression(SchemaObject<?> schemaObject, Operator operator) {
            this.schemaObject = schemaObject;
            this.operator = operator;
        }

        public boolean evaluate() {
            boolean exists = schemaObject.exists();
            return switch (operator) {
                case EXISTS -> exists;
                case NOT_EXISTS -> !exists;
            };
        }
    }


}
