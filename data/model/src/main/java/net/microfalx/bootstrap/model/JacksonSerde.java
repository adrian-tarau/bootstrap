package net.microfalx.bootstrap.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

import java.io.IOException;

/**
 * Various serializers and deserializers for JSON content using the Jackson library.
 */
class JacksonSerde {

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
        net.microfalx.bootstrap.core.utils.Jackson.registerSerde(Resource.class, new ResourceSerializer(), new ResourceDeserializer());
    }

}
