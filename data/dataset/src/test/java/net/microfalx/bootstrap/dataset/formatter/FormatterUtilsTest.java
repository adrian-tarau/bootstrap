package net.microfalx.bootstrap.dataset.formatter;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static net.microfalx.lang.TimeUtils.UTC_ZONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void formatIntegers() {
        assertEquals("1", FormatterUtils.basicFormatting(1, null));
        assertEquals("100", FormatterUtils.basicFormatting(100, null));
        assertEquals("100,000", FormatterUtils.basicFormatting(100_000, null));
    }

    @Test
    void formatDecimals() {
        assertEquals("0.01", FormatterUtils.basicFormatting(0.01, null));
        assertEquals("0.1", FormatterUtils.basicFormatting(0.1, null));
        assertEquals("1", FormatterUtils.basicFormatting(1.0, null));
        assertEquals("100.34", FormatterUtils.basicFormatting(100.34, null));
        assertEquals("1,200.74", FormatterUtils.basicFormatting(1200.74, null));
    }

    @Test
    void formatDecimalsWith3Digits() {
        Formattable formattable = mock(Formattable.class);
        when(formattable.unit()).thenReturn(Formattable.Unit.NONE);
        when(formattable.prettyPrint()).thenReturn(true);
        when(formattable.minimumFractionDigits()).thenReturn(3);
        when(formattable.maximumFractionDigits()).thenReturn(3);
        assertEquals("0.010", FormatterUtils.basicFormatting(0.01f, formattable));
        assertEquals("0.100", FormatterUtils.basicFormatting(0.1f, formattable));
        assertEquals("1.000", FormatterUtils.basicFormatting(1.0f, formattable));
        assertEquals("100.340", FormatterUtils.basicFormatting(100.34f, formattable));
        assertEquals("1,200.740", FormatterUtils.basicFormatting(1200.74f, formattable));

        when(formattable.maximumFractionDigits()).thenReturn(1);
        assertEquals("0.0", FormatterUtils.basicFormatting(0.01f, formattable));
        assertEquals("0.1", FormatterUtils.basicFormatting(0.1f, formattable));
        assertEquals("1.0", FormatterUtils.basicFormatting(1.0f, formattable));
        assertEquals("100.3", FormatterUtils.basicFormatting(100.34f, formattable));
        assertEquals("1,200.7", FormatterUtils.basicFormatting(1200.74f, formattable));
    }

}