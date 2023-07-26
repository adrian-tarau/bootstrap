package net.microfalx.bootstrap.web.dataset.formatter;

import net.microfalx.bootstrap.web.dataset.annotation.Formattable;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import java.text.NumberFormat;

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
    public static String basicFormatting(Object value, Formattable formattable) {
        if (value instanceof String) {
            String valueAsString = (String) value;
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
        } else {
            return ObjectUtils.toString(value);
        }
    }

}
