package net.microfalx.bootstrap.dataset;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSetUtilsTest {

    @Test
    void getStep() {
        assertEquals(Duration.ofSeconds(30), DataSetUtils.getStep(ZonedDateTime.now().minusMinutes(15), ZonedDateTime.now()));
        assertEquals(Duration.ofSeconds(60), DataSetUtils.getStep(ZonedDateTime.now().minusMinutes(30), ZonedDateTime.now()));
        assertEquals(Duration.ofMinutes(2), DataSetUtils.getStep(ZonedDateTime.now().minusHours(1), ZonedDateTime.now()));
        assertEquals(Duration.ofMinutes(5), DataSetUtils.getStep(ZonedDateTime.now().minusHours(3), ZonedDateTime.now()));
        assertEquals(Duration.ofMinutes(20), DataSetUtils.getStep(ZonedDateTime.now().minusHours(11), ZonedDateTime.now()));
        assertEquals(Duration.ofMinutes(45), DataSetUtils.getStep(ZonedDateTime.now().minusHours(24), ZonedDateTime.now()));
        assertEquals(Duration.ofMinutes(25), DataSetUtils.getStep(ZonedDateTime.now().minusHours(24), ZonedDateTime.now(), 50));
    }

}