package net.microfalx.bootstrap.core.utils;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonTest {

    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);
    private static final LocalTime TIME = LocalTime.of(8, 37, 43);
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2024, 6, 1, 12, 37, 43);
    private static final ZonedDateTime ZONED_DATE_TIME = DATE_TIME.atZone(ZoneId.systemDefault());
    private static final OffsetDateTime OFFSET_DATE_TIME = DATE_TIME.atOffset(ZoneOffset.ofHours(-5));
    private static final Duration DURATION = Duration.ofHours(4);

    @Test
    void dateTimeSerialization() {
        assertEquals("\"2024-06-01\"", Json.asString(DATE));
        assertEquals("\"08:37:43\"", Json.asString(TIME));
        assertEquals("\"2024-06-01T12:37:43\"", Json.asString(DATE_TIME));
        assertEquals("\"2024-06-01T12:37:43-04:00\"", Json.asString(ZONED_DATE_TIME));
        assertEquals("\"2024-06-01T12:37:43-05:00\"", Json.asString(OFFSET_DATE_TIME));
        assertEquals("\"PT4H\"", Json.asString(DURATION));
    }

}