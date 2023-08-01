package net.microfalx.bootstrap.model;

import jodd.typeconverter.TypeConverterManager;
import net.microfalx.lang.ClassUtils;

/**
 * Various utilities around fields.
 */
public class FieldUtils {

    static final TypeConverterManager TYPE_CONVERTER_MANAGER = TypeConverterManager.get();

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    static <T> T from(Object value, Class<T> target) {
        try {
            return TYPE_CONVERTER_MANAGER.convertType(value, target);
        } catch (Exception e) {
            throw new InvalidDataTypeExpression("Data conversion failure for '" + value + "' to type '" + ClassUtils.getName(target) + "'", e);
        }
    }
}
