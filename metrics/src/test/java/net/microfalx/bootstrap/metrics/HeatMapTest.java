package net.microfalx.bootstrap.metrics;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeatMapTest {

    private static final int POINTS = 50;
    private static final int MATRICES = 100;
    private static final String[] ATTRIBUTES = {"type", "owner", "source", "target"};

    private static final Random random = ThreadLocalRandom.current();

    @Test
    void fromMatrices() {
        Collection<HeatMap> heatMaps = HeatMap.create(generateMatrixes());
        assertEquals(4, heatMaps.size());
        assertEquals(5, heatMaps.iterator().next().getSeries().size());
    }

    private Collection<Matrix> generateMatrixes() {
        Collection<Matrix> matrixes = new ArrayList<>();
        for (int i = 0; i < MATRICES; i++) {
            matrixes.add(generateMatrix());
        }
        return matrixes;
    }

    private Matrix generateMatrix() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("type", getNextValue());
        attributes.put("owner", getNextValue());
        attributes.put("source", getNextValue());
        attributes.put("target", getNextValue());
        Metric metric = Metric.create("count", attributes);
        LocalDateTime end = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime start = end.minusHours(24);
        Duration duration = Duration.between(start, end).dividedBy(POINTS);
        List<Value> values = new ArrayList<>();
        while (start.isBefore(end)) {
            values.add(Value.create(start, random.nextInt(5)));
            start = start.plus(duration);
        }
        return Matrix.create(metric, values);
    }

    private String getNextValue() {
        return Integer.toString(random.nextInt(5));
    }

}