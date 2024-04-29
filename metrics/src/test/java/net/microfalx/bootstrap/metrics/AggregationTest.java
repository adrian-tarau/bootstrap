package net.microfalx.bootstrap.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.TimeUtils.FIVE_MINUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationTest {

    private static final long TIMESTAMP = LocalDateTime.of(2024, 4, 28, 2, 31, 12)
            .toInstant(ZoneOffset.UTC).toEpochMilli();

    private final Metric m1 = Metric.create("test", Map.of("l1", "v1"));
    private final Metric m2 = Metric.create("test", Map.of("l1", "v1", "l2", "v2"));
    private final Metric m3 = Metric.create("test", Map.of("l1", "v1", "l2", "v2"));

    private Aggregation aggregation;

    @BeforeEach
    void setup() {
        aggregation = new Aggregation();
        assertEquals(Aggregation.Type.SUM, aggregation.getType());
    }

    @Test
    void oneTimeSeries5MinutesSum() {
        addValues(m1);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(1, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(7, matrix.getValues().size());
        assertEquals(3, matrix.getFirst().get().getValue());
        assertEquals(30, matrix.getLast().get().getValue());
    }

    @Test
    void oneTimeSeries5MinutesMin() {
        aggregation.setType(Aggregation.Type.MIN);
        addValues(m1);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(1, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(7, matrix.getValues().size());
        assertEquals(1, matrix.getFirst().get().getValue());
        assertEquals(10, matrix.getLast().get().getValue());
    }

    @Test
    void oneTimeSeries5MinutesMax() {
        aggregation.setType(Aggregation.Type.MAX);
        addValues(m1);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(1, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(7, matrix.getValues().size());
        assertEquals(2, matrix.getFirst().get().getValue());
        assertEquals(20, matrix.getLast().get().getValue());
    }

    @Test
    void oneTimeSeries5MinutesAvg() {
        aggregation.setType(Aggregation.Type.AVG);
        addValues(m1);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(1, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(7, matrix.getValues().size());
        assertEquals(1.5, matrix.getFirst().get().getValue());
        assertEquals(15, matrix.getLast().get().getValue());
    }

    @Test
    void oneTimeSeries15MinutesSum() {
        aggregation.setStep(ofMinutes(15));
        addValues(m1);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(1, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(3, matrix.getValues().size());
        assertEquals(9, matrix.getFirst().get().getValue());
        assertEquals(30, matrix.getLast().get().getValue());
    }

    @Test
    void twoTimeSeries5MinutesSum() {
        addValues(m1);
        addValues(m2);
        Collection<Matrix> matrixes = aggregation.toMatrixes();
        assertEquals(2, matrixes.size());
        Matrix matrix = matrixes.iterator().next();
        assertEquals("test", matrix.getMetric().getName());
        assertEquals(7, matrix.getValues().size());
        assertEquals(3, matrix.getFirst().get().getValue());
        assertEquals(30, matrix.getLast().get().getValue());
    }

    private void addValues(Metric metric) {
        aggregation.add(metric, Value.create(TIMESTAMP + 1000, 1));
        aggregation.add(metric, Value.create(TIMESTAMP + 10000, 2));
        aggregation.add(metric, Value.create(TIMESTAMP + 5 * FIVE_MINUTE + 1000, 10));
        aggregation.add(metric, Value.create(TIMESTAMP + 5 * FIVE_MINUTE + 3000, 20));
        aggregation.add(metric, Value.create(TIMESTAMP + 2 * FIVE_MINUTE + 1000, 1));
        aggregation.add(metric, Value.create(TIMESTAMP + 2 * FIVE_MINUTE + 3000, 2));
        aggregation.add(metric, Value.create(TIMESTAMP + FIVE_MINUTE + 1000, 1));
        aggregation.add(metric, Value.create(TIMESTAMP + FIVE_MINUTE + 3000, 2));
        aggregation.add(metric, Value.create(TIMESTAMP + 4 * FIVE_MINUTE + 1000, 1));
        aggregation.add(metric, Value.create(TIMESTAMP + 4 * FIVE_MINUTE + 3000, 2));
        aggregation.add(metric, Value.create(TIMESTAMP + 3 * FIVE_MINUTE + 1000, 1));
        aggregation.add(metric, Value.create(TIMESTAMP + 3 * FIVE_MINUTE + 3000, 2));
        aggregation.add(metric, Value.create(TIMESTAMP + 6 * FIVE_MINUTE + 1000, 10));
        aggregation.add(metric, Value.create(TIMESTAMP + 6 * FIVE_MINUTE + 3000, 20));
    }

}