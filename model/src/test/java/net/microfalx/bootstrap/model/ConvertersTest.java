package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvertersTest {

    @Test
    void zonedDateTime() {
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00.000Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T00:00-04:00[America/New_York]"), Converters.from("2023-09-03", ZonedDateTime.class));
    }

    @Test
    void offsetDateTime() {
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00.000Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T00:00-04:00"), Converters.from("2023-09-03", OffsetDateTime.class));
    }

}