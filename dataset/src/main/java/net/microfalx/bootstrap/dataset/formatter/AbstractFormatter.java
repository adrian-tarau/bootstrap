package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ObjectUtils;

/**
 * Base class for all formatters
 *
 * @param <M> the model type
 * @param <F> the field type
 * @param <T> the value type
 */
@Formattable
public abstract class AbstractFormatter<M, F extends Field<M>, T> implements Formatter<M, F, T> {

    @Override
    public String format(T value, F field, M model) {
        if (value == null) return getFormattable(field).nullValue();
        if (ObjectUtils.isEmpty(value)) return getFormattable(field).emptyValue();
        if (value instanceof Number && ((Number) value).doubleValue() < 0) return getFormattable(field).negativeValue();
        return doFormat(value, field, model);
    }

    /**
     * Subclasses perform the formatting.
     *
     * @param value the value
     * @param field the field
     * @param model the model
     * @return the formatted value
     */
    protected abstract String doFormat(T value, F field, M model);

    /**
     * Returns the associated {@link Formattable} annotation.
     *
     * @param field the field
     * @return the value
     */
    protected final Formattable getFormattable(F field) {
        Formattable annotation = field.findAnnotation(Formattable.class);
        if (annotation == null) annotation = AnnotationUtils.getAnnotation(this, Formattable.class);
        return annotation;
    }
}
