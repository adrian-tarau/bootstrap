package net.microfalx.bootstrap.ai.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.core.utils.Json;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.MediaContent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.resource.ResourceUtils.loadAsString;

@Getter
@ToString
public class MessageImpl implements Message {

    private final String id = UUID.randomUUID().toString();
    private final Type type;
    private final List<Content> contents = new ArrayList<>();
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    public static Message create(org.springframework.ai.chat.messages.Message message) {
        return new MessageImpl(getType(message), getContent(message));
    }

    public static Message create(Message.Type type, String text) {
        return new MessageImpl(type, List.of(ContentImpl.from(text)));
    }

    public static Message create(Message.Type type, Collection<Content> content) {
        return new MessageImpl(type, content);
    }

    public MessageImpl() {
        this(Type.USER);
    }

    public MessageImpl(Type type) {
        this(type, emptyList());
    }

    private MessageImpl(Type type, Collection<Content> contents) {
        requireNonNull(type);
        requireNonNull(contents);
        this.type = type;
        this.contents.addAll(contents);
    }

    @Override
    @JsonIgnore
    public List<Content> getContent() {
        return unmodifiableList(contents);
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    @JsonIgnore
    public String getText() {
        if (contents.isEmpty()) {
            return StringUtils.EMPTY_STRING;
        } else {
            StringBuilder sb = new StringBuilder();
            for (Content content : contents) {
                sb.append(loadAsString(content.getResource()));
            }
            return sb.toString();
        }
    }

    private static List<Content> getContent(org.springframework.ai.chat.messages.Message message) {
        if (message == null) return emptyList();
        if (message instanceof SystemMessage systemMessage) {
            return List.of(ContentImpl.from(systemMessage.getText()));
        } else if (message instanceof MediaContent mediaContent) {
            return getContent(mediaContent);
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            return List.of(ContentImpl.from(toolResponseMessage.getText()));
        } else {
            return List.of(ContentImpl.from(message.getText()));
        }
    }

    private static List<Content> getContent(MediaContent mediaContent) {
        if (mediaContent.getMedia().isEmpty()) {
            return List.of(ContentImpl.from(mediaContent.getText()));
        } else {
            return mediaContent.getMedia().stream().map(ContentImpl::from).toList();
        }
    }

    private static Message.Type getType(org.springframework.ai.chat.messages.Message message) {
        return switch (message) {
            case SystemMessage m -> Type.SYSTEM;
            case ToolResponseMessage m -> Type.TOOL;
            case UserMessage m -> Type.USER;
            case AssistantMessage m -> Type.MODEL;
            case null, default -> Type.CUSTOM;
        };
    }

    public static final class Serializer extends StdSerializer<Message> {

        public Serializer() {
            super(Message.class);
        }

        @Override
        public void serialize(Message value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("type", value.getType().name());
            gen.writeFieldName("content");
            provider.defaultSerializeValue(value.getContent(), gen);
            gen.writeEndObject();
        }
    }

    public static final class Deserializer extends StdDeserializer<Message> {

        public Deserializer() {
            super(Message.class);
        }

        @Override
        public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            ObjectMapper codec = (ObjectMapper) p.getCodec();
            JsonNode node = codec.readTree(p);
            Message.Type type = EnumUtils.fromName(Message.Type.class, node.get("type").asText(), Type.CUSTOM);
            List<Content> content = codec.convertValue(node.get("content"), CONTENT_LIST_TYPE);
            return MessageImpl.create(type, content);
        }
    }


    static {
        Json.registerSerde(Message.class, new Serializer(), new Deserializer());
    }

    private static final TypeReference<List<Content>> CONTENT_LIST_TYPE = new TypeReference<List<Content>>() {
    };


}
