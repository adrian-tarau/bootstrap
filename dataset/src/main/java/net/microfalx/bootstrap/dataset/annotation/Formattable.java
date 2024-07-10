package net.microfalx.bootstrap.dataset.annotation;

import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.formatter.Formatter;
import net.microfalx.bootstrap.model.Field;

import java.lang.annotation.*;
import java.time.Duration;
import java.util.StringJoiner;

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
     * Based on the unit of measure, a different formatter will be used. When a throughput is requested, the created or modified
     * field (in this order) will be used to calculated the duration.
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
     * Returns the provider for the alert.
     *
     * @return the provider
     */
    Class<? extends AlertProvider> alert() default AlertProvider.class;

    /**
     * A unit of measure for a formatter
     */
    enum Unit {

        /**
         * No unit of measure
         */
        NONE(false, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatNumber(Object)}
         */
        COUNT(false, false),

        /**
         * Formats the integer as is, no pretty-print
         */
        INTEGER(false, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatBytes(Object)}
         */
        BYTES(false, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatThroughput(Object, Duration, String)}
         */
        THROUGHPUT_BYTES(true, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatThroughput(Object, Duration, String)}
         */
        THROUGHPUT_REQUESTS(true, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatThroughput(Object, Duration, String)}
         */
        THROUGHPUT_TRANSACTIONS(true, false),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with nanosecond unit
         */
        NANO_SECOND(false, true),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with microsecond unit
         */
        MICRO_SECOND(false, true),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with millisecond unit
         */
        MILLI_SECOND(false, true),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with second unit
         */
        SECOND(false, true),

        /**
         * Uses {@link net.microfalx.lang.FormatterUtils#formatDuration(Object)} with second unit
         */
        MINUTE(false, true);

        private boolean throughput;
        private boolean time;

        Unit(boolean throughput, boolean time) {
            this.throughput = throughput;
            this.time = time;
        }

        public boolean isThroughput() {
            return throughput;
        }

        public boolean isTime() {
            return time;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Unit.class.getSimpleName() + "[", "]")
                    .add("name=" + name())
                    .add("throughput=" + throughput)
                    .add("time=" + time)
                    .toString();
        }
    }

    /**
     * An interface which allows a formatter to attach an alert to a field.
     *
     * @param <M> the model
     * @param <F> the field
     * @param <T> the value
     */
    interface AlertProvider<M, F extends Field<M>, T> {

        /**
         * Formats the value.
         *
         * @param value the value of the field
         * @param field the field
         * @param model the model
         * @return the formatted value
         */
        Alert provide(T value, F field, M model);
    }
}
