package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.model.Types;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageImplTest {

    @Test
    void serializeObject() {
        String json = Types.asString(createMessage());
        Message message = Types.asObject(json, Message.class);
        assertNotNull(message);
    }

    @Test
    void serializeList() {
        String json = Types.asString(List.of(createMessage(), createMessage(), createMessage()));
        Collection<Message> messages = Types.asCollection(json, Message.class);
        assertNotNull(messages);
        assertEquals(3, messages.size());
    }

    private Message createMessage() {
        Message.Type type = Message.Type.values()[ThreadLocalRandom.current().nextInt(Message.Type.values().length)];
        return MessageImpl.create(type, "Text");
    }

}