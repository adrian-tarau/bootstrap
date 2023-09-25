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
     * A value for "value not available"
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
     * Returns the custom formatter to be used for a field.
     *
     * @return the formatter
     */
    Class<? extends Formatter> formatter() default Formatter.class;
}
