package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ObjectUtils;

import static net.microfalx.lang.EnumUtils.*;
import static net.microfalx.lang.ObjectUtils.asString;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    private Class<Enum> enumClass;

    public EnumFormatter() {
    }

    public EnumFormatter(Class<Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected String doFormat(T value, F field, M model) {
        Enum enumValue = null;
        if (value instanceof String) {
            enumValue = fromName(getEnumClass(field), (String) value, null);
            if (enumValue == null) return (String) value;

        } else if (value instanceof Number) {
            enumValue = fromOrdinal(getEnumClass(field), ((Number) value).intValue(), null);
            if (enumValue == null) return ObjectUtils.toString(value);
        }
        return enumValue != null ? toName(enumValue) : asString(value);
    }

    private Class<Enum> getEnumClass(F field) {
        return enumClass != null ? enumClass : (Class<Enum>) field.getDataClass();
    }
}
