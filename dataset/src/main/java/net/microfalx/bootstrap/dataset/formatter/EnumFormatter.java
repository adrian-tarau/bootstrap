package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ObjectUtils;

import static net.microfalx.lang.EnumUtils.*;
import static net.microfalx.lang.ObjectUtils.asString;

@SuppressWarnings("unchecked")
public class EnumFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    @SuppressWarnings("rawtypes")
    @Override
    protected String doFormat(T value, F field, M model) {
        Enum enumValue = null;
        if (value instanceof String) {
            enumValue = fromName((Class<Enum>) field.getDataClass(), (String) value, null);
            if (enumValue == null) return (String) value;

        } else if (value instanceof Number) {
            enumValue = fromOrdinal((Class<Enum>) field.getDataClass(), ((Number) value).intValue(), null);
            if (enumValue == null) return ObjectUtils.toString(value);
        }
        return enumValue != null ? toName(enumValue) : asString(value);
    }
}
