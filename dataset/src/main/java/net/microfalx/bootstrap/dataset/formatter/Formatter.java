package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.model.Field;

/**
 * An interface used by data set fields to apply custom formats to create the display value.
 */
public interface Formatter<M, F extends Field<M>, T> {

    /**
     * Formats the value.
     *
     * @param value the value of the field
     * @param field the field
     * @param model the model
     * @return the formatted value
     */
    String format(T value, F field, M model);

    /**
     * Parses the formatted value and returns the original value.
     *
     * @param text  the formatted value
     * @param field the field
     * @return the value
     */
    T parse(String text, F field);
}
