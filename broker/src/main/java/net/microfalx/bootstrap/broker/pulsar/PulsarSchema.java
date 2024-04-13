package net.microfalx.bootstrap.broker.pulsar;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import net.microfalx.bootstrap.broker.BrokerException;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.lang.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.pulsar.client.impl.schema.AbstractSchema;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.shade.io.netty.buffer.ByteBufInputStream;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

@SuppressWarnings({"resource", "unchecked"})
public class PulsarSchema<T> extends AbstractSchema<T> {

    private final Topic topic;
    private final static SchemaInfo SCHEMA_INFO = new SchemaInfoImpl();

    public PulsarSchema(Topic topic) {
        this.topic = topic;
    }

    @Override
    public T decode(byte[] bytes, byte[] schemaVersion) {
        return doDecode(bytes);
    }

    @Override
    public T decode(org.apache.pulsar.shade.io.netty.buffer.ByteBuf byteBuf) {
        ByteBufInputStream stream = new ByteBufInputStream(byteBuf);
        try {
            byte[] bytes = IOUtils.getInputStreamAsBytes(stream);
            return doDecode(bytes);
        } catch (IOException e) {
            throw new BrokerException("Failed to decode event from topic '" + topic.getName() + "'", e);
        }
    }

    @Override
    public byte[] encode(T message) {
        return switch (topic.getFormat()) {
            case RAW -> new ByteArraySerializer().serialize(topic.getName(), (byte[]) message);
            case JSON -> new JsonSerializer<>().serialize(topic.getName(), message);
            case AVRO -> new KafkaAvroSerializer().serialize(topic.getName(), message);
            default -> throw new BrokerException("Unsupported format: " + topic.getFormat());
        };
    }

    @Override
    public SchemaInfo getSchemaInfo() {
        return SCHEMA_INFO;
    }

    private T doDecode(byte[] bytes) {
        try {
            return switch (topic.getFormat()) {
                case RAW -> (T) new ByteArrayDeserializer().deserialize(topic.getName(), bytes);
                case JSON -> (T) new JsonDeserializer<>().deserialize(topic.getName(), bytes);
                case AVRO -> (T) new KafkaAvroDeserializer().deserialize(topic.getName(), bytes);
                default -> throw new BrokerException("Unsupported format: " + topic.getFormat());
            };
        } catch (Exception e) {
            throw new BrokerException("Failed to decode event from topic '" + topic.getName() + "'", e);
        }
    }

    static class SchemaInfoImpl implements SchemaInfo {

        private final long timestamp = System.currentTimeMillis();

        @Override
        public String getName() {
            return "Bootstrap Schema";
        }

        @Override
        public byte[] getSchema() {
            return StringUtils.EMPTY.getBytes();
        }

        @Override
        public SchemaType getType() {
            return SchemaType.BYTES;
        }

        @Override
        public Map<String, String> getProperties() {
            return emptyMap();
        }

        @Override
        public String getSchemaDefinition() {
            return StringUtils.EMPTY;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
}
