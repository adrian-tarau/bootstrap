package net.microfalx.bootstrap.dataset.annotation;

import net.microfalx.bootstrap.dataset.formatter.Formatter;

import java.lang.annotation.*;

/**
 * An annotation used to provide formatting rules or custom formatters to fields.
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Formattable {

    /**
     * A value for "value not available"
     */
    String NA = "-";

    /**
     * A value for "value is negative"
     */
    String AUTO = "$AUTO$";

    /**
     * Returns the maximum number of lines displayed for a field (if the field type is STRING)
     *
     * @return the number of lines, -1 for unlimited
     */
    int maximumLines() default -1;

    /**
     * Returns the maximum number of characters displayed for a field (if the field type is STRING)
     *
     * @return the number of characters, -1 for unlimited
     */
    int maximumLength() default -1;

    /**
     * Returns the string to be used when the value is null.
     *
     * @return the value
     */
    String nullValue() default NA;

    /**
     * Returns the string to be used when the value is empty.
     *
     * @return the value
     */
    String emptyValue() default NA;

    /**
     * Returns the string to be used when the value is negative.
     *
     * @return the value
     */
    String negativeValue() default AUTO;

    /**
     * Uses a human-readable format to make it easier to read the values.
     * <p>
     * For numbers, it uses locale specific thousands and fractional separators, for temporal uses custom format patterns
     * to make the formatted value shorter.
     *
     * @return {@code true} to use pretty-print formatters, {@code false} to use basic formatters
     */
    boolean prettyPrint() default true;

    /**
     * Returns the unit of measure for this formatter.
     * <p>
     * Based on the unit of measure, a different formatter will be used
     *
     * @return the unit
     */
    Unit unit() default Unit.NONE;

    /**
     * Returns the custom formatter to be used for a field.
     *
     * @return the formatter
     */
    Class<? extends Formatter> formatter() default Formatter.class;

    /**
     * A unit of measure for a formatter
     */
    enum Unit {

        /**
         * No unit of measure
         */
        NONE,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatNumber(Object)}
         */
        COUNT,

        /**
         * Formats the integer as is, no pretty-print
         */
        INTEGER,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatBytes(Object)}
         */
        BYTES,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with nanosecond unit
         */
        NANO_SECOND,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with microsecond unit
         */
        MICRO_SECOND,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with millisecond unit
         */
        MILLI_SECOND,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with second unit
         */
        SECOND,

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with second unit
         */
        MINUTE
    }
}
