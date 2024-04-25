package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.core.i18n.I18n;
import net.microfalx.bootstrap.dataset.DataSetUtils;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ObjectUtils;

import static net.microfalx.lang.EnumUtils.fromName;
import static net.microfalx.lang.EnumUtils.fromOrdinal;
import static net.microfalx.lang.ObjectUtils.asString;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    private Class<Enum> enumClass;
    private I18n i18n;

    public EnumFormatter() {
    }

    public EnumFormatter(Class<Enum> enumClass) {
        this.enumClass = enumClass;
    }

    public I18n getI18n() {
        return i18n;
    }

    public void setI18n(I18n i18n) {
        this.i18n = i18n;
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
        } else if (value instanceof Enum) {
            enumValue = (Enum) value;
        }
        if (enumValue != null) {
            return DataSetUtils.getDisplayValue(i18n, enumValue);
        } else {
            return asString(value);
        }
    }

    @Override
    public T parse(String text, F field) {
        return (T) EnumUtils.fromName(enumClass, text);
    }

    private Class<Enum> getEnumClass(F field) {
        return enumClass != null ? enumClass : (Class<Enum>) field.getDataClass();
    }
}
