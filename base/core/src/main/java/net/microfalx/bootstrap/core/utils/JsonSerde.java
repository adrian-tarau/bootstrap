package net.microfalx.bootstrap.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.microfalx.lang.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Additional serilizers/deserializes for Jackson.
 */
public class JsonSerde {

    public static class SystemZoneLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
}
