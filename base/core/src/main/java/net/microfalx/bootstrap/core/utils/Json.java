package net.microfalx.bootstrap.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.Resource;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;

/**
 * A facade for Jackson ObjectMapper and related classes.
 * <p>
 * When deserializing, the value can be a {@link Reader}, {@link File},
 * {@link InputStream} or {@link Resource} or a String or byte[].
 * <p>
 * Any null or empty string or empty bytes[] will be deserialized as a null value or empty collection.
 */
public class Json {

    private static volatile ObjectMapper cachedObjectMapper;
    private static volatile SimpleModule cachedModule;
    private static final Collection<Module> modules = new CopyOnWriteArrayList<>();

    /**
     * Registers a new module.
     *
     * @param module the module to register
     */
    public static void registerModule(Module module) {
        requireNonNull(module);
        modules.add(module);
        reset();
    }

    /**
     * Registers a new serializer and deserializer for a specific type.
     *
     * @param type         the type for which the serializer and deserializer should be registered
     * @param serializer   the serializer to register
     * @param deserializer the deserializer to register
     * @param <T>          the type for which the serializer and deserializer should be registered
     */
    public static synchronized <T> void registerSerde(Class<T> type, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        requireNonNull(type);
        requireNonNull(serializer);
        requireNonNull(deserializer);
        SimpleModule module = getModule();
        module.addSerializer(type, serializer);
        module.addDeserializer(type, deserializer);
        reset();
    }

    /**
     * Converts the object to a String.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static String asString(Object value) {
        if (value == null) return null;
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Converts the object to an array of bytes.
     *
     * @param value the value to convert
     * @return the converted value
     */
    public static byte[] asBytes(Object value) {
        if (value == null) return null;
        try {
            return getObjectMapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    /**
     * Converts a JSON object to a Collection.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Collection<?> asCollection(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyList();
        return getObjectMapper().readValue(getReader(value), Collection.class);
    }

    /**
     * Converts a JSON object to a Collection of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> Collection<T> asCollection(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyList();
        ObjectMapper objectMapper = getObjectMapper();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(Collection.class, elementType);
        return objectMapper.readValue(getReader(value), collectionType);
    }

    /**
     * Converts a JSON object to a Set.
     *
     * @param value the value
     * @return the converted collection
     */
    public static Set<?> asSet(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptySet();
        ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.readValue(getReader(value), Set.class);
    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> Set<T> asSet(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptySet();
        ObjectMapper objectMapper = Json.getObjectMapper();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(Set.class, elementType);
        return objectMapper.readValue(getReader(value), collectionType);
    }

    /**
     * Converts a JSON object to a Map.
     *
     * @param value the value
     * @return the converted collection
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> asMap(Object value) throws IOException {
        if (ObjectUtils.isEmpty(value)) return Collections.emptyMap();
        ObjectMapper objectMapper = Json.getObjectMapper();
        return objectMapper.readValue(getReader(value), Map.class);

    }

    /**
     * Converts a JSON object to a Set of a given type.
     *
     * @param value       the value
     * @param elementType the element class
     * @param <T>         the element type
     * @return the converted collection
     */
    public static <T> T asObject(Object value, Class<T> elementType) throws IOException {
        if (ObjectUtils.isEmpty(value)) return null;
        return getObjectMapper().readValue(getReader(value), elementType);

    }

    /**
     * Returns the object mapper.
     *
     * @return a non-null instance
     */
    public static ObjectMapper getObjectMapper() {
        if (cachedObjectMapper == null) {
            synchronized (Json.class) {
                if (cachedObjectMapper == null) {
                    cachedObjectMapper = createObjectMapper();
                }
            }
        }
        return cachedObjectMapper;
    }

    static synchronized void reset() {
        cachedObjectMapper = null;
    }

    private static Reader getReader(Object value) throws IOException {
        requireNonNull(value);
        return switch (value) {
            case Reader reader -> reader;
            case String string -> new StringReader(string);
            case File file -> IOUtils.getBufferedReader(new FileReader(file));
            case InputStream inputStream -> new InputStreamReader(inputStream);
            case byte[] bytes -> new InputStreamReader(new ByteArrayInputStream(bytes));
            case Resource resource -> resource.getReader();
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        };
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.registerModule(getModule());
        for (Module module : modules) {
            objectMapper.registerModule(module);
        }

        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new JsonSerde.SystemZoneLocalDateTimeSerializer());
        timeModule.addDeserializer(LocalDateTime.class, new JsonSerde.SystemZoneLocalDateTimeDeserializer());
        objectMapper.registerModule(timeModule);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        return objectMapper;
    }

    static synchronized SimpleModule getModule() {
        if (cachedModule == null) cachedModule = new SimpleModule();
        return cachedModule;
    }
}
