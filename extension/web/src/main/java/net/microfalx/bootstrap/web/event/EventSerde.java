package net.microfalx.bootstrap.web.event;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ReflectionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joor.Reflect;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Serialization/deserialization support for events.
 */
public class EventSerde {

    private ObjectMapper objectMapper;

    /**
     * Reads content and converts it to a JSON tree.
     *
     * @param payload the event JSON
     * @return the JSON tree
     */
    public JsonNode read(String payload) {
        try {
            return getMapper().readTree(payload);
        } catch (IOException e) {
            throw new EventException("Cannot read event payload", e);
        }
    }

    /**
     * Serializes an event to JSON.
     *
     * @param event the event
     * @return the JSON representation
     * @throws IOException if an I/O error occurs
     */
    public String serialize(Event event) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = objectMapper.createGenerator(writer);
        Serializer serializer = new Serializer();
        serializer.serialize(event, generator, objectMapper.getSerializerProvider());
        return writer.toString();
    }

    /**
     * Deserializes an event from a JSON tree.
     *
     * @param node        the JSON tree
     * @param eventClass  the event class
     * @param application the application identifier
     * @param <E>         the event type
     * @return the event
     */
    public <E> E deserialize(JsonNode node, Class<E> eventClass) {
        requireNonNull(node);
        requireNonNull(eventClass);
        String eventName = EventUtilities.getEventName(node);
        try {
            Constructor<E> constructor = eventClass.getDeclaredConstructor();
            E event = constructor.newInstance();
            ArrayNode argumentsNode = (ArrayNode) node.get("arguments");
            if (argumentsNode != null) {
                MutableInt index = new MutableInt();
                getFields(eventClass).forEach(field -> {
                    updateField(event, field, argumentsNode.get(index.getAndIncrement()));
                });
            }
            return event;
        } catch (NoSuchMethodException e) {
            throw new EventException("Event class '" + eventClass + "' does not have a default constructor", e);
        } catch (Exception e) {
            throw new EventException("Event '" + eventName + "' (" + ClassUtils.getName(eventClass) + ") cannot be decoded", e);
        }
    }

    private <E> void updateField(E event, Field field, JsonNode valueNode) {
        Object value = getMapper().convertValue(valueNode, field.getType());
        try {
            field.set(event, value);
        } catch (IllegalAccessException e) {
            throw new EventException("Failed to set event argument for field '" + field.getName() + "'", e);
        }
    }

    private Stream<Field> getFields(Class<?> eventClass) {
        return ReflectionUtils.openFields(eventClass).stream().filter(field -> !RESERVED_FIELDS.contains(field.getName()));
    }

    private ObjectMapper getMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        }
        return objectMapper;
    }

    static final class Deserializer extends JsonDeserializer<Event> {

        private final Event event;

        public Deserializer(Event event) {
            requireNonNull(event);
            this.event = event;
        }

        @Override
        public Event deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return null;
        }
    }

    static final class Serializer extends JsonSerializer<Event> {

        @Override
        public void serialize(Event value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Collection<Reflect> fields = Reflect.onClass(value.getClass()).fields().values();
            //serializers.findTypedValueSerializer()
            for (Reflect field : fields) {
                Object fieldValue = field.get();
                gen.writeObjectField("a", fieldValue);
            }
        }
    }

    private static final Set<String> RESERVED_FIELDS = Set.of("id", "name", "application");
}
