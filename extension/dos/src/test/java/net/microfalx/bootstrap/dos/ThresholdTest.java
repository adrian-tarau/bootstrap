package net.microfalx.bootstrap.dos;

import org.junit.jupiter.api.Test;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ThresholdTest {

    @Test
    void parseThreshold() {
        Threshold threshold = DosUtils.parseThreshold("2 r/s, 5m");
        assertEquals(2, threshold.getRequestRate());
        assertEquals(ofMinutes(5), threshold.getBlockingPeriod());
    }
}