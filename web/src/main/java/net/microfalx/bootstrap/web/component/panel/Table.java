package net.microfalx.bootstrap.web.component.panel;

import net.microfalx.bootstrap.dataset.formatter.FormatterUtils;
import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public final class Table extends BasePanel<Table> {

    private final List<Column> columns = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();

    public static Table create(String... columns) {
        Table table = new Table();
        table.addColumns(columns);
        return table;
    }

    public static Table create(int columnCount) {
        Table table = new Table();
        for (int i = 0; i < columnCount; i++) {
            table.addColumn(new Column(i, null));
        }
        return table;
    }

    private Table() {
    }

    public boolean hasHeader() {
        for (Column column : columns) {
            if (StringUtils.isNotEmpty(column.getText())) return true;
        }
        return false;
    }

    public final List<Column> getColumns() {
        return unmodifiableList(columns);
    }

    public List<Row> getRows() {
        return unmodifiableList(rows);
    }

    public Table addColumn(Column column) {
        requireNonNull(column);
        columns.add(column);
        return this;
    }

    public Table addColumns(String... colums) {
        for (String colum : colums) {
            addColumn(new Column(this.columns.size(), colum));
        }
        return this;
    }

    public Table addRow(Object... components) {
        requireNonNull(components);
        if (components.length != columns.size()) {
            throw new IllegalArgumentException("Received " + components.length + " components but table has "
                    + columns.size() + " columns");
        }
        rows.add(new Row(Arrays.asList(components)));
        return this;
    }

    public Table addRows(Consumer<Table> consumer) {
        requireNonNull(consumer);
        consumer.accept(this);
        return this;
    }

    @Override
    public String toString() {
        return "Table{" +
                "columns=" + columns.size() +
                ", rows=" + rows.size() +
                "} " + super.toString();
    }

    public static class Row {

        private List<Object> values;

        public Row(List<Object> values) {
            requireNonNull(values);
            this.values = values;
        }

        public boolean isComponent(Column column) {
            return getValue(column) instanceof Component<?>;
        }

        public Object getDisplayValue(Column column) {
            return FormatterUtils.basicFormatting(getValue(column), null);
        }

        public Object getValue(Column column) {
            requireNonNull(column);
            return values.get(column.getIndex());
        }

        public Collection<Object> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "Row{" +
                    "values=" + values +
                    '}';
        }
    }

    public static class Column {

        private final int index;
        private String text;

        public Column(int index, String text) {
            this.index = index;
            this.text = text;
        }

        public int getIndex() {
            return index;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "index=" + index +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}
