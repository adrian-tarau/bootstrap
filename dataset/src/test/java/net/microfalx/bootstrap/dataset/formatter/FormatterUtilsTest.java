package net.microfalx.bootstrap.dataset.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatterUtilsTest {

    private static final LocalDateTime dateTime = LocalDateTime.of(2024, 2, 22, 10, 45);

    @BeforeEach
    void before() {
        FormatterUtils.setTimeZone(null);
    }

    @Test
    void formatTemporals() {
        assertEquals("02/22/2024 10:45:00", FormatterUtils.basicFormatting(dateTime, null));
        FormatterUtils.setTimeZone(ZoneId.of("America/Los_Angeles"));
        assertEquals("02/22/2024 07:45:00", FormatterUtils.basicFormatting(dateTime, null));
    }

}