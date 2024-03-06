package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.FormatterUtils;

/**
 * A formatter which takes a number of milliseconds and converts them to units.
 *
 * @param <M> the model type
 * @param <F> the field type
 * @param <T> the data type
 */
public class MillisecondFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    @Override
    protected String doFormat(T value, F field, M model) {
        return FormatterUtils.formatDuration(value);
    }

    @Override
    public T parse(String text, F field) {
        throw new UnsupportedOperationException();
    }
}
