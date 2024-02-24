package net.microfalx.bootstrap.dataset.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static net.microfalx.lang.TimeUtils.UTC_ZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatterUtilsTest {

    private static final LocalDateTime dateTime = LocalDateTime.of(2024, 2, 22, 10, 45);

    @BeforeEach
    void before() {
        TimeZone.setDefault(TimeZone.getDefault());
        FormatterUtils.setTimeZone(null);
    }

    @Test
    void formatTemporals() {
        assertEquals("02/22/2024 10:45:00", FormatterUtils.basicFormatting(dateTime, null));
        assertEquals("02/22/2024 05:45:00", FormatterUtils.basicFormatting(dateTime.atZone(UTC_ZONE), null));
        FormatterUtils.setTimeZone(ZoneId.of("America/Los_Angeles"));
        assertEquals("02/22/2024 07:45:00", FormatterUtils.basicFormatting(dateTime, null));
        assertEquals("02/22/2024 02:45:00", FormatterUtils.basicFormatting(dateTime.atZone(UTC_ZONE), null));
    }

    @Test
    void formatTemporalsFromUTC() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        assertEquals("02/22/2024 10:45:00", FormatterUtils.basicFormatting(dateTime, null));
        FormatterUtils.setTimeZone(ZoneId.of("America/New_York"));
        assertEquals("02/22/2024 05:45:00", FormatterUtils.basicFormatting(dateTime, null));
    }

}