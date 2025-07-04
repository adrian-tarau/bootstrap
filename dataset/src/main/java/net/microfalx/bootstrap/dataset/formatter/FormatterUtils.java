package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Locale;

import static net.microfalx.lang.TimeUtils.*;

/**
 * Various utilities around formatters.
 */
public class FormatterUtils {

    /**
     * Holds the current time zone. If the thread runs in the context of a user request, it should hold the
     * time zone of the user (regardless how is that obtained).
     */
    private static ThreadLocal<ZoneId> TIME_ZONE = ThreadLocal.withInitial(ZoneId::systemDefault);

    /**
     * Holds the current zone. If the thread runs in the context of a user request, , it should hold the
     * locale of the user (regardless how is that obtained).
     */
    private static ThreadLocal<Locale> LOCALE = ThreadLocal.withInitial(Locale::getDefault);

    /**
     * Returns the time zone associated with the current thread.
     *
     * @return a non-null instance
     */
    public static ZoneId getTimeZone() {
        return TIME_ZONE.get();
    }

    /**
     * Changes the time zone associated with the current thread.
     *
     * @param locale the time zone, null to set to system default
     */
    public static void setTimeZone(ZoneId locale) {
        if (locale == null) {
            TIME_ZONE.remove();
        } else {
            TIME_ZONE.set(locale);
        }
    }


    /**
     * Returns the locale associated with the current thread.
     *
     * @return a non-null instance
     */
    public static Locale getLocale() {
        return LOCALE.get();
    }

    /**
     * Changes the locale associated with the current thread.
     *
     * @param locale the locale, null to set to system default
     */
    public static void setLocale(Locale locale) {
        if (locale == null) {
            LOCALE.remove();
        } else {
            LOCALE.set(locale);
        }
    }

    /**
     * Applies basic formatting rules.
     *
     * @param value the value
     * @return the formatted value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String basicFormatting(Object value) {
        return basicFormatting(value, null);
    }

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
                return org.apache.commons.lang3.StringUtils.abbreviateMiddle(valueAsString, "...",formattable.maximumLength());
            } else {
                return valueAsString;
            }
        } else if (value instanceof Number) {
            if (formattable != null) {
                Formattable.Unit unit = formattable.unit();
                if (!formattable.prettyPrint()) {
                    return ObjectUtils.toString(value);
                } else if (unit.isThroughput()) {
                    if (unit == Formattable.Unit.THROUGHPUT_BYTES) {
                        return net.microfalx.lang.FormatterUtils.formatThroughput(value, "b");
                    } else if (unit == Formattable.Unit.THROUGHPUT_REQUESTS) {
                        return net.microfalx.lang.FormatterUtils.formatThroughput(value, "r");
                    } else if (unit == Formattable.Unit.THROUGHPUT_TRANSACTIONS) {
                        return net.microfalx.lang.FormatterUtils.formatThroughput(value, "t");
                    } else {
                        throw new IllegalArgumentException("Unhandled unit: " + unit);
                    }
                } else if (unit.isTime()) {
                    if (unit == Formattable.Unit.MICRO_SECOND) {
                        float floatValue = ((Number) value).floatValue() / MICROSECONDS_IN_MILLISECONDS;
                        return net.microfalx.lang.FormatterUtils.formatDuration(floatValue);
                    } else if (unit == Formattable.Unit.MILLI_SECOND) {
                        return net.microfalx.lang.FormatterUtils.formatDuration(value);
                    } else if (unit == Formattable.Unit.SECOND) {
                        value = ((Number) value).longValue() * MILLISECONDS_IN_SECOND;
                        return net.microfalx.lang.FormatterUtils.formatDuration(value);
                    } else if (unit == Formattable.Unit.MINUTE) {
                        value = ((Number) value).longValue() * MILLISECONDS_IN_MINUTE;
                        return net.microfalx.lang.FormatterUtils.formatDuration(value);
                    } else if (unit == Formattable.Unit.NANO_SECOND) {
                        float floatValue = ((Number) value).floatValue() / NANOSECONDS_IN_MILLISECONDS;
                        return net.microfalx.lang.FormatterUtils.formatDuration(floatValue);
                    } else {
                        throw new IllegalArgumentException("Unhandled unit: " + unit);
                    }
                } else if (unit == Formattable.Unit.COUNT) {
                    return net.microfalx.lang.FormatterUtils.formatNumber(value);
                } else if (unit == Formattable.Unit.BYTES) {
                    return net.microfalx.lang.FormatterUtils.formatBytes(value);
                } else if (!Formattable.AUTO.equals(formattable.negativeValue()) && ((Number) value).doubleValue() < 0) {
                    return formattable.NA;
                }
            }
            int minimumFractionDigits = -1;
            int maximumFractionDigits = -1;
            if (formattable != null) {
                minimumFractionDigits = formattable.minimumFractionDigits();
                maximumFractionDigits = formattable.maximumFractionDigits();
                minimumFractionDigits = Math.min(minimumFractionDigits, maximumFractionDigits);
            }
            if (value instanceof Float || value instanceof Double) {
                NumberFormat numberInstance = NumberFormat.getNumberInstance();
                numberInstance.setRoundingMode(RoundingMode.HALF_UP);
                if (minimumFractionDigits >= 0) numberInstance.setMinimumFractionDigits(minimumFractionDigits);
                if (maximumFractionDigits >= 0) numberInstance.setMaximumFractionDigits(maximumFractionDigits);
                return numberInstance.format(((Number) value).doubleValue());
            } else {
                return NumberFormat.getIntegerInstance().format(((Number) value).longValue());
            }
        } else if (value instanceof Enum) {
            return EnumUtils.toLabel((Enum) value);
        } else if (value instanceof Temporal) {
            return net.microfalx.lang.FormatterUtils.formatTemporal((Temporal) value, getTimeZone());
        } else if (value instanceof Duration) {
            return net.microfalx.lang.FormatterUtils.formatDuration(value, Formattable.NA, false);
        } else {
            return ObjectUtils.toString(value);
        }
    }

}
