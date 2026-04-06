package net.microfalx.bootstrap.ai.core;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.bootstrap.core.utils.Jackson;
import net.microfalx.lang.EnumUtils;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.springframework.ai.content.Media;

import java.io.IOException;
import java.net.URL;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.emptyIfNull;

@ToString
public class ContentImpl implements Content {

    private final Type type;
    private final Resource resource;

    static Content from(Media media) {
        requireNonNull(media);
        return new ContentImpl(getType(media), getResource(media));
    }

    public static Content from(String text) {
        text = emptyIfNull(text);
        return new ContentImpl(Content.Type.TEXT, Resource.text(text));
    }

    public static Content from(Resource resource) {
        return from(Content.Type.TEXT, resource);
    }

    public static Content from(Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        return new ContentImpl(type, resource);
    }

    private ContentImpl(Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        this.type = type;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public long getSize() {
        try {
            return resource.length();
        } catch (IOException e) {
            return -1;
        }
    }

    private static Type getType(Media media) {
        MimeType mimeType = MimeType.get(media.getMimeType().getType());
        if (mimeType.isText()) {
            return Type.TEXT;
        } else if (MimeType.IMAGE.equals(mimeType)) {
            return Type.IMAGE;
        } else {
            return Type.VIDEO;
        }
    }

    private static Resource getResource(Media media) {
        Resource resource;
        if (media.getData() instanceof String) {
            resource = Resource.url((String) media.getData());
        } else if (media.getData() instanceof byte[]) {
            resource = Resource.bytes((byte[]) media.getData());
        } else {
            throw new IllegalArgumentException("Unsupported media: " + media);
        }
        MimeType mimeType = MimeType.get(media.getMimeType().getType());
        return resource.withName(media.getName()).withMimeType(mimeType);
    }

    private static Resource create(URL url, String base64Encoded) {
        if (url != null) {
            return Resource.url(url);
        } else if (base64Encoded != null) {
            return Resource.base64Encoded(base64Encoded);
        } else {
            throw new IllegalArgumentException("Either URL or base64Encoded must be provided");
        }
    }

    private static final class Serializer extends StdSerializer<Content> {

        public Serializer() {
            super(Content.class);
        }

        @Override
        public void serialize(Content value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("type", value.getType().name());
            gen.writeFieldName("resource");
            provider.defaultSerializeValue(value.getResource(), gen);
            gen.writeEndObject();
        }
    }

    private static final class Deserializer extends StdDeserializer<Content> {

        public Deserializer() {
            super(Content.class);
        }

        @Override
        public Content deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            ObjectCodec codec = p.getCodec();
            ObjectNode node = codec.readTree(p);
            Content.Type type = EnumUtils.fromName(Content.Type.class, node.get("type").asText(), Type.DOCUMENT);
            Resource resource = codec.treeToValue(node.get("resource"), Resource.class);
            return ContentImpl.from(type, resource);
        }
    }

    static {
        Jackson.registerSerde(Content.class, new Serializer(), new Deserializer());
    }
}
