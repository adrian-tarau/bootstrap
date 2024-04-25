package net.microfalx.bootstrap.model;

import jodd.time.TimeUtil;
import jodd.typeconverter.TypeConverter;
import jodd.typeconverter.TypeConverterManager;
import jodd.typeconverter.impl.LocalDateTimeConverter;
import jodd.util.StringUtil;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;

import java.time.*;
import java.time.temporal.Temporal;

/**
 * Hosts a collection of converters.
 */
class Converters {

    static final TypeConverterManager TYPE_CONVERTER_MANAGER = TypeConverterManager.get();

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T from(Object value, Class<T> target) {
        if (target == null) return null;
        try {
            if (target.isEnum()) {
                return (T) FieldConverters.toEnum((Class<Enum>) target, value);
            } else if (ClassUtils.isSubClassOf(target, Temporal.class)) {
                return (T) toTemporal(value, (Class<Temporal>) target);
            } else if (ClassUtils.isSubClassOf(target, Number.class) && ObjectUtils.isEmpty(value)) {
                return target.isPrimitive() ? from(0, target) : null;
            } else {
                return convert(value, target);
            }
        } catch (InvalidDataTypeExpression e) {
            throw e;
        } catch (Exception e) {
            return throwConversionException(value, target, e);
        }
    }

    /**
     * Converts special string to a temporal.
     *
     * @param value  the value
     * @param target the target
     * @param <T>    the subset of temporal
     * @return the temporal
     */
    private static <T extends Temporal> Temporal toTemporal(Object value, Class<T> target) {
        if (value instanceof Temporal) return (Temporal) value;
        if ("now".equals(value)) {
            return convert(LocalDateTime.now(), target);
        } else if ("today".equals(value)) {
            return convert(LocalDate.now().atStartOfDay(), target);
        } else if ("yesterday".equals(value)) {
            return convert(LocalDate.now().minusDays(1).atStartOfDay(), target);
        } else if ("last week".equals(value)) {
            return convert(LocalDate.now().minusDays(7).atStartOfDay(), target);
        } else if ("next week".equals(value)) {
            return convert(LocalDate.now().plusDays(7).atStartOfDay(), target);
        } else {
            return convert(value, target);
        }
    }

    private static <T> T convert(Object value, Class<T> target) {
        try {
            return TYPE_CONVERTER_MANAGER.convertType(value, target);
        } catch (Exception e) {
            return throwConversionException(value, target, e);
        }
    }

    private static <T> T throwConversionException(Object value, Class<T> target, Throwable throwable) {
        throw new InvalidDataTypeExpression("Data conversion failure for '" + value + "' to type '" + ClassUtils.getName(target) + "'", throwable);
    }

    private static final int DATE_TIME_MIN_LENGTH = 12;

    static {
        TYPE_CONVERTER_MANAGER.register(ZonedDateTime.class, new ZonedDateTimeConverter());
        TYPE_CONVERTER_MANAGER.register(OffsetDateTime.class, new OffsetDateTimeConverter());
    }

    static class ZonedDateTimeConverter implements TypeConverter<ZonedDateTime> {

        private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

        @Override
        public ZonedDateTime convert(Object value) {
            if (value == null) return null;
            if (value instanceof ZonedDateTime) {
                return (ZonedDateTime) value;
            } else if (value instanceof OffsetDateTime) {
                return ((OffsetDateTime) value).toZonedDateTime();
            } else if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if (!StringUtil.containsOnlyDigits(stringValue)) {
                    return stringValue.length() >= DATE_TIME_MIN_LENGTH ? ZonedDateTime.parse(stringValue)
                            : LocalDate.parse(stringValue).atStartOfDay(ZoneId.systemDefault());
                } else {
                    return TimeUtil.fromMilliseconds(Long.parseLong(stringValue)).atZone(ZoneId.systemDefault());
                }
            } else {
                LocalDateTime localDateTime = converter.convert(value);
                return localDateTime.atZone(ZoneId.systemDefault());
            }
        }
    }

    static class OffsetDateTimeConverter implements TypeConverter<OffsetDateTime> {

        private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

        @Override
        public OffsetDateTime convert(Object value) {
            if (value == null) return null;
            if (value instanceof OffsetDateTime) {
                return (OffsetDateTime) value;
            } else if (value instanceof ZonedDateTime) {
                return ((ZonedDateTime) value).toOffsetDateTime();
            } else if (value instanceof String) {
                String stringValue = ((String) value).trim();
                if (!StringUtil.containsOnlyDigits(stringValue)) {
                    return stringValue.length() >= DATE_TIME_MIN_LENGTH ? OffsetDateTime.parse(stringValue)
                            : LocalDate.parse(stringValue).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
                } else {
                    return TimeUtil.fromMilliseconds(Long.parseLong(stringValue)).atZone(ZoneId.systemDefault()).toOffsetDateTime();
                }
            } else {
                LocalDateTime localDateTime = converter.convert(value);
                return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
        }
    }
}
