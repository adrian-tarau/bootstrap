package net.microfalx.bootstrap.logger;

import net.microfalx.lang.ExceptionUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggerEventTest {

    @Test
    void getCorrelationId() {
        Set<String> correlationIds = new HashSet<>();
        correlationIds.add(createEvent().build().getCorrelationId());
        correlationIds.add(createEvent().message("This is an event 2").build().getCorrelationId());
        correlationIds.add(createEvent().level(LoggerEvent.Level.TRACE).build().getCorrelationId());
        assertEquals(3, correlationIds.size());
    }

    private LoggerEvent.LoggerEventBuilder createEvent() {
        return LoggerEvent.builder().name(LoggerEvent.class.getName())
                .timestamp(123456789).level(LoggerEvent.Level.ERROR).threadName("main")
                .message("This is an event")
                .exceptionStackTrace(ExceptionUtils.getStackTrace(new IOException("Test")));
    }

}