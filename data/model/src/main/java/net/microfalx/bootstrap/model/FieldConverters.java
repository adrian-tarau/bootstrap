package net.microfalx.bootstrap.model;

import net.microfalx.lang.EnumUtils;

/**
 * A collection of Jodd data conversions
 */
public class FieldConverters {

    /**
     * Converts to an enum.
     *
     * @param type  the enum class
     * @param value the value
     * @param <E>   the enum type
     * @return the enum
     */
    @SuppressWarnings("unchecked")
    static final <E extends Enum<E>> E toEnum(Class<E> type, Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Enum) {
            return (E) value;
        } else if (value instanceof Number) {
            return EnumUtils.fromOrdinal(type, ((Number) value).intValue());
        } else {
            return EnumUtils.fromName(type, ((String) value));
        }

    }
}
