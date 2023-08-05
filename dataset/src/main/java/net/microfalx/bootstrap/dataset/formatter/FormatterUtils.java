package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import java.text.NumberFormat;
import java.time.temporal.Temporal;

/**
 * Various utilities around formatters.
 */
public class FormatterUtils {

    /**
     * Applies basic formatting rules.
     *
     * @param value       the value
     * @param formattable the rule for formatting
     * @return the formatted value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String basicFormatting(Object value, Formattable formattable) {
        if (value instanceof String valueAsString) {
            if (formattable != null && formattable.maximumLines() > 0) {
                return StringUtils.getMaximumLines(valueAsString, formattable.maximumLines());
            } else if (formattable != null && formattable.maximumLength() > 0) {
                return org.apache.commons.lang3.StringUtils.abbreviate(valueAsString, formattable.maximumLength());
            } else {
                return valueAsString;
            }
        } else if (value instanceof Number) {
            if (value instanceof Float || value instanceof Double) {
                return NumberFormat.getNumberInstance().format(((Number) value).doubleValue());
            } else {
                return NumberFormat.getIntegerInstance().format(((Number) value).longValue());
            }
        } else if (value instanceof Enum) {
            return EnumUtils.toName((Enum)value);
        } else if (value instanceof Temporal) {
            return net.microfalx.lang.FormatterUtils.formatTemporal((Temporal) value);
        } else {
            return ObjectUtils.toString(value);
        }
    }

}
