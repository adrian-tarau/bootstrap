package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.EnumUtils.*;
import static net.microfalx.lang.ObjectUtils.asString;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    private Class<Enum> enumClass;
    private I18nService i18nService;

    public EnumFormatter() {
    }

    public EnumFormatter(Class<Enum> enumClass) {
        this.enumClass = enumClass;
    }

    public I18nService getI18nService() {
        return i18nService;
    }

    public void setI18nService(I18nService i18nService) {
        this.i18nService = i18nService;
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
        if (i18nService != null) {
            String text = i18nService.getText(getEnumKey(enumValue), false);
            if (StringUtils.isNotEmpty(text)) return text;
        }
        return enumValue != null ? toLabel(enumValue) : asString(value);
    }

    @Override
    public T parse(String text, F field) {
        return (T) EnumUtils.fromName(enumClass, text);
    }

    private String getEnumKey(Enum value) {
        return "enum." + ClassUtils.getName(value).toLowerCase() + "." + value.name().toLowerCase();
    }

    private Class<Enum> getEnumClass(F field) {
        return enumClass != null ? enumClass : (Class<Enum>) field.getDataClass();
    }
}
