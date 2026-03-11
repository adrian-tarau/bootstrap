package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A facade for Jackson ObjectMapper and related classes.
 */
public class Jackson {

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

    static synchronized void reset() {
        cachedObjectMapper = null;
    }

    static ObjectMapper getObjectMapper() {
        if (cachedObjectMapper == null) {
            synchronized (Jackson.class) {
                if (cachedObjectMapper == null) {
                    cachedObjectMapper = createObjectMapper();
                }
            }
        }
        return cachedObjectMapper;
    }

    private static ObjectMapper createObjectMapper() {
        JacksonSerde.initialize();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(getModule());
        for (Module module : modules) {
            objectMapper.registerModule(module);
        }
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    static synchronized SimpleModule getModule() {
        if (cachedModule == null) cachedModule = new SimpleModule();
        return cachedModule;
    }
}
