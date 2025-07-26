package net.microfalx.bootstrap.web.util;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import jodd.util.StringUtil;
import lombok.ToString;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.FormatterUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING_ARRAY;
import static net.microfalx.lang.StringUtils.replaceFirst;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * A class which can generate an HTML table.
 */
@ToString
public class TableGenerator {

    private Type type = Type.BOOTSTRAP;
    private boolean small;
    private boolean links;
    private boolean strict = true;
    private final Set<String> classes = new HashSet<>();
    private final List<Column> columns = new ArrayList<>();
    private final List<Object[]> rows = new ArrayList<>();

    private static final String SP2 = "\n  ";
    private static final String SP4 = "\n    ";
    private static final String SP6 = "\n      ";
    private static final String SP8 = "\n        ";

    private static final String CHECKBOX_CHECKED = "<i class='fa-regular fa-square-check'></i>";
    private static final String CHECKBOX_UNCHECKED = "<i class='fa-regular fa-square'></i>";
    private static final String LINK = "<i class='fa-solid fa-link' title='${title}'></i>";

    public TableGenerator addClass(final String className) {
        if (StringUtil.isNotBlank(className)) classes.add(className);
        return this;
    }

    public TableGenerator setType(Type type) {
        this.type = type;
        return this;
    }

    public boolean isSmall() {
        return small;
    }

    public TableGenerator setSmall(boolean small) {
        this.small = small;
        return this;
    }

    public boolean isLinks() {
        return links;
    }

    public TableGenerator setLinks(boolean links) {
        this.links = links;
        return this;
    }

    public boolean isStrict() {
        return strict;
    }

    public TableGenerator setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public List<Column> getColumns() {
        return unmodifiableList(columns);
    }

    public List<Object[]> getRows() {
        return unmodifiableList(rows);
    }

    public TableGenerator addColumn(String name) {
        requireNonNull(name);
        return addColumn(new Column(name));
    }

    public TableGenerator addColumns(String... names) {
        requireNonNull(names);
        for (String name : names) {
            addColumn(name);
        }
        return this;
    }

    public TableGenerator addColumn(Column column) {
        requireNonNull(column);
        columns.add(column);
        return this;
    }

    public TableGenerator addRow(Object... values) {
        requireNonNull(values);
        if (values.length != columns.size()) {
            if (strict) {
                throw new IllegalArgumentException("Number of values (" + values.length + " is different from number of columns (" + columns.size() + ")");
            } else {
                Object[] newValues = new Object[columns.size()];
                System.arraycopy(values, 0, newValues, 0, Math.min(newValues.length, values.length));
                values = newValues;
            }
        }
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            DataType dataType = guessDataType(value);
            columns.get(i).upgradeDataType(dataType);
        }
        rows.add(values);
        return this;
    }

    public TableGenerator addRow(Collection<?> values) {
        requireNonNull(values);
        rows.add(values.toArray());
        return this;
    }

    public TableGenerator addRows(Resource resource) throws IOException {
        ArgumentUtils.requireNonNull(resource);
        CSVFormat format = CSVFormat.Builder.create(CSVFormat.RFC4180)
                .setIgnoreHeaderCase(true).setLenientEof(!strict).build();
        Iterator<CSVRecord> records = format.parse(resource.getReader()).iterator();
        if (!records.hasNext()) return this;
        CSVRecord record = records.next();
        addColumns(record.values());
        while (records.hasNext()) {
            record = records.next();
            addRow((Object[]) record.values());
        }
        return this;
    }

    public String generate() {
        StringBuilder builder = new StringBuilder(8000);
        builder.append("<table ").append("class='").append(getClasses(this.classes, getTableClasses())).append("'>");
        appendHeader(builder);
        appendBody(builder);
        builder.append("\n</table>");
        return builder.toString();
    }

    private void appendHeader(StringBuilder builder) {
        builder.append(SP2).append("<thead>").append(SP4).append("<tr>");
        for (Column column : columns) {
            builder.append(SP6).append("<th scope='col' ").append(getCellClasses(true, column)).append(">");
            builder.append(column.getName()).append("</th>");
        }
        builder.append(SP4).append("</tr>").append(SP2).append("</thead>");
    }

    private void appendBody(StringBuilder builder) {
        builder.append(SP2).append("<tbody>");
        for (Object[] row : rows) {
            builder.append(SP4).append("<tr>");
            for (int i = 0; i < row.length; i++) {
                Column column = columns.get(i);
                builder.append(SP6).append("<td ").append(getCellClasses(false, column)).append(">");
                builder.append(toString(column, row[i]));
                builder.append("</td>");
            }
            builder.append(SP4).append("</tr>");
        }
        builder.append(SP2).append("</tbody>");
    }

    private String getClasses(Collection<String> classes, String... extraClasses) {
        Collection<String> allClasses = new LinkedHashSet<>();
        allClasses.addAll(classes);
        allClasses.addAll(Arrays.asList(extraClasses));
        return String.join(StringUtils.SPACE, allClasses);
    }

    private String[] getTableClasses() {
        String[] classes = switch (type) {
            case PLAIN -> EMPTY_STRING_ARRAY;
            case BOOTSTRAP -> new String[]{"table", "table-striped", "table-bordered", "table-hover"};
        };
        Collection<String> allClasses = new LinkedHashSet<>(Arrays.asList(classes));
        if (small && type == Type.BOOTSTRAP) allClasses.add("table-sm");
        return allClasses.toArray(new String[0]);
    }

    private boolean isBoolean(Object value) {
        if (value instanceof Boolean) return true;
        String valueAsString = value.toString();
        return "true".equalsIgnoreCase(valueAsString) || "false".equalsIgnoreCase(valueAsString);
    }

    private boolean isInteger(Object value) {
        if (!(value instanceof Number)) return false;
        if (value instanceof Integer || value instanceof Long) return true;
        return Longs.tryParse(value.toString()) != null;
    }

    private boolean isNumber(Object value) {
        if (!(value instanceof Number)) return false;
        if (value instanceof Float || value instanceof Double) return true;
        return Doubles.tryParse(value.toString()) != null;
    }

    private DataType guessDataType(Object value) {
        if (ObjectUtils.isEmpty(value)) return DataType.STRING;
        if (isBoolean(value)) {
            return DataType.BOOLEAN;
        } else if (isInteger(value)) {
            return DataType.INTEGER;
        } else if (isNumber(value)) {
            return DataType.NUMBER;
        } else {
            return DataType.STRING;
        }
    }

    private String toString(Column column, Object value) {
        return switch (column.getDataType()) {
            case BOOLEAN -> toString(StringUtils.asBoolean(value, false));
            case INTEGER -> FormatterUtils.formatNumber(value);
            case NUMBER -> FormatterUtils.formatNumber(value, 2);
            default -> toStringDefault(column, value);
        };
    }

    private String toStringDefault(Column column, Object value) {
        String valueAsString = ObjectUtils.toString(value);
        if (type == Type.BOOTSTRAP) {
            if (isLink(valueAsString)) {
                return replaceFirst(LINK, "${title}", escapeHtml4(valueAsString));
            }
        }
        return valueAsString;
    }

    private String toString(boolean value) {
        if (type == Type.BOOTSTRAP) {
            return value ? CHECKBOX_CHECKED : CHECKBOX_UNCHECKED;
        } else {
            return Boolean.toString(value);
        }
    }

    private boolean isLink(String value) {
        return links && value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }

    private String getCellClasses(boolean forHeader, Column column, String... extraClasses) {
        Collection<String> classes = new LinkedHashSet<>();
        switch (column.getAlign()) {
            case LEFT -> classes.add("text-left");
            case CENTER -> classes.add("text-center");
            case RIGHT -> classes.add("text-right");
        }
        if (extraClasses.length > 0) classes.addAll(Arrays.asList(extraClasses));
        if (forHeader) classes.add("text-nowrap");
        return "class='" + String.join(" ", classes) + "'";
    }

    @ToString
    public static class Column {

        private final String name;
        private Align align = Align.LEFT;
        private DataType dataType = DataType.STRING;

        public Column(String name) {
            requireNonNull(name);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Align getAlign() {
            return switch (dataType) {
                case BOOLEAN -> Align.CENTER;
                case INTEGER, NUMBER -> Align.RIGHT;
                default -> align;
            };
        }

        public Column setAlign(Align align) {
            requireNonNull(align);
            this.align = align;
            return this;
        }

        public DataType getDataType() {
            return dataType;
        }

        public Column setDataType(DataType dataType) {
            requireNonNull(dataType);
            this.dataType = dataType;
            return this;
        }

        private void upgradeDataType(DataType dataType) {
            if (this.dataType == DataType.STRING && dataType != DataType.STRING) {
                this.dataType = dataType;
            }
        }
    }

    public enum Type {
        PLAIN, BOOTSTRAP
    }

    public enum Align {
        LEFT, CENTER, RIGHT
    }

    public enum DataType {
        BOOLEAN, INTEGER, NUMBER, STRING
    }
}
