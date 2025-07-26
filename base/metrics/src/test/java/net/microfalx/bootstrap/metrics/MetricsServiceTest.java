package net.microfalx.bootstrap.metrics;

import net.microfalx.metrics.Query;
import net.microfalx.metrics.QueryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest()
@ContextConfiguration(classes = MetricsService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MetricsServiceTest {

    @Autowired
    private MetricsService metricsService;

    @Test
    void initialize() {
        assertEquals(1, metricsService.getRepositories().size());
    }

    @Test
    void queryNoRepo() {
        Assertions.assertThrows(MetricException.class, () -> {
            metricsService.query(Query.create("a", "a"));
        });
    }

    @Test
    void queryInvalid() {
        Assertions.assertThrows(QueryException.class, () -> {
            metricsService.query(Query.create("test", "invalid"));
        });
    }

    @Test
    void queryValid() {
        assertFalse(metricsService.query(Query.create("test", "good")).isEmpty());
    }

}