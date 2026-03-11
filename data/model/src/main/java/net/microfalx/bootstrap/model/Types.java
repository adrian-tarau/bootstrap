package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.Resource;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A facade for data type conversions.
 * <p>
 * The string representation of a collection or map or a complex object is expected to be in JSON format. JSON content
 * can be provided as a String, Reader, File, InputStream or Resource.
 */
public class Types {

    private Types() {
    }

    /**
     * Converts an object to a target type.
     *
     * @param value  the value to convert
     * @param target the target class
     * @param <T>    the type of the target value
     * @return the converted value
     */
    public static <T> T from(Object value, Class<T> target) {
        return Converters.from(value, target);
    }

    /**
     * Converts the object to a String.
     * <p>
     * If complex object is provided, it will be converted to JSON string.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static String asString(Object value) {
        if (value == null) return null;
        return from(value, String.class);
    }

    /**
     * Converts a JSON object to a Collection.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Collection<?> asCollection(Object value) {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyList();
        ObjectMapper objectMapper = Converters.getObjectMapper();
        try {
            return objectMapper.readValue(getReader(value), Collection.class);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Collection of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> Collection<T> asCollection(Object value, Class<T> elementType) {
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(Collection.class, elementType);
        try {
            return objectMapper.readValue(getReader(value), collectionType);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to collection of " + elementType.getName(), e);
        }
    }

    /**
     * Converts a JSON object to a Set.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Set<?> asSet(Object value) {
        if (ObjectUtils.isEmpty(value)) return Collections.emptySet();
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        try {
            return objectMapper.readValue(getReader(value), Set.class);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    static <T> Set<T> asSet(Object value, Class<T> elementType) {
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(Set.class, elementType);
        try {
            return objectMapper.readValue(getReader(value), collectionType);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to collection of " + elementType.getName(), e);
        }
    }

    /**
     * Converts a JSON object to a Map.
     *
     * @param value the value
     * @return the converted collection
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> asMap(Object value) {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyMap();
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        try {
            return objectMapper.readValue(getReader(value), Map.class);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to collection", e);
        }
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> T asObject(Object value, Class<T> elementType) {
        ObjectMapper objectMapper = Jackson.getObjectMapper();
        try {
            return objectMapper.readValue(getReader(value), elementType);
        } catch (IOException e) {
            throw new ModelException("Failed to convert value to object of " + elementType.getName(), e);
        }
    }

    private static Reader getReader(Object value) throws IOException {
        return switch (value) {
            case Reader reader -> reader;
            case String string -> new StringReader(string);
            case File file -> IOUtils.getBufferedReader(new FileReader(file));
            case InputStream inputStream -> new InputStreamReader(inputStream);
            case Resource resource -> resource.getReader();
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        };
    }
}
