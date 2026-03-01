package net.microfalx.bootstrap.ai.core;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.ai.api.Content;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.lang.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

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
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public static Message create(org.springframework.ai.chat.messages.Message message) {
        return new MessageImpl(getType(message), getContent(message));
    }

    public static Message create(Message.Type type, String text) {
        return new MessageImpl(type, List.of(ContentImpl.from(text)));
    }

    public MessageImpl(Type type) {
        this(type, emptyList());
    }

    public MessageImpl(Type type, Collection<Content> contents) {
        requireNonNull(type);
        requireNonNull(contents);
        this.type = type;
        this.contents.addAll(contents);
    }

    @Override
    public List<Content> getContent() {
        return unmodifiableList(contents);
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
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
        if (message instanceof SystemMessage systemMessage) {
            return List.of(ContentImpl.from(systemMessage.getText()));
        } else if (message instanceof UserMessage userMessage) {
            return userMessage.getMedia().stream().map(ContentImpl::from).toList();
        } else if (message instanceof AssistantMessage assistantMessage) {
            return assistantMessage.getMedia().stream().map(ContentImpl::from).toList();
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            return List.of(ContentImpl.from(toolResponseMessage.getText()));
        } else if (message == null) {
            return emptyList();
        } else {
            throw new IllegalArgumentException();
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

}
