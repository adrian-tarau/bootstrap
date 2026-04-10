package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import net.microfalx.bootstrap.core.utils.Json;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Various serializers and deserializers for JSON content using the Jackson library.
 */
public class JsonSerde {

    public static class SystemZoneLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {

            if (value == null) {
                gen.writeNull();
            } else {
                ZonedDateTime zoned = value.atZone(ZoneId.systemDefault());
                // ISO format with zone (recommended)
                gen.writeString(zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
        }
    }

    public static class SystemZoneLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            if (StringUtils.isEmpty(text)) return null;
            // Parse as OffsetDateTime (safer than ZonedDateTime for JSON)
            OffsetDateTime odt = OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    private static class ResourceSerializer extends JsonSerializer<Resource> {

        @Override
        public void serialize(Resource resource, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            byte[] content = resource.loadAsBytes();
            gen.writeBinaryField("content", content);
            gen.writeStringField("fileName", resource.getFileName());
            gen.writeNumberField("lastModified", resource.lastModified());
            gen.writeEndObject();
        }
    }

    private static class ResourceDeserializer extends JsonDeserializer<Resource> {

        @Override
        public Resource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            byte[] content = node.get("content").binaryValue();
            String fileName = node.get("fileName").asText();
            long lastModified = node.get("lastModified").asLong();
            return MemoryResource.create(content, fileName, lastModified);
        }
    }

    static void initialize() {
        Json.registerSerde(Resource.class, new ResourceSerializer(), new ResourceDeserializer());
    }

}
