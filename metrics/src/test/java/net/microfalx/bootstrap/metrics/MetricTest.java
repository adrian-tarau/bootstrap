package net.microfalx.bootstrap.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetricTest {

    private Metric metric;

    @BeforeEach
    void setup() {
        metric = Metric.create("Test", "l1", "v1", "l2", "v2");
    }

    @Test
    void create() {
        assertEquals("unnamed", Metric.create().getId());
        assertEquals("UNNAMED", Metric.create().getName());
        assertEquals(0, Metric.create().getLabels().size());
    }

    @Test
    void getLabels() {
        assertEquals("test", metric.getId());
        assertEquals("Test", metric.getName());
        assertEquals(2, metric.getLabels().size());
    }

    @Test
    void getLabel() {
        assertEquals("v1", metric.getLabel("l1"));
        assertNull(metric.getLabel("l3"));
    }

    @Test
    void hasLabel() {
        assertTrue(metric.hasLabel("l1"));
        assertFalse(metric.hasLabel("l3"));
    }

    @Test
    void deduplication() {
        Metric m1 = Metric.get("test", Map.of("l1", "v1"));
        Metric m2 = Metric.get("test", Map.of("l1", "v1", "l2", "v2"));
        Metric m3 = Metric.get("test", Map.of("l1", "v1", "l2", "v2"));
        assertNotEquals(m1, m2);
        assertNotSame(m1, m2);
        assertEquals(m2, m3);
        assertSame(m2, m3);

    }
}