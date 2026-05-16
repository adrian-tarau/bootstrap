package net.microfalx.bootstrap.trace;

import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.bootstrap.trace.startup.StartupRecorderTask;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@ContextConfiguration(classes = TraceService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TraceServiceTest {

    @MockitoBean private ThreadPool threadPool;
    @MockitoBean private StoreService storeService;

    @Autowired private ApplicationContext applicationContext;
    @Autowired private TraceService traceService;

    @Test
    void initialize() {
        assertTrue(applicationContext instanceof ConfigurableApplicationContext);
    }

    @Test
    void getTimeline() {
        assertThat(traceService.getStartupTimeline().getSteps().size()).isGreaterThan(0);
    }

    @Test
    void afterStartup() {
        ReflectionTestUtils.invokeMethod(traceService, "recordMetrics", Duration.ofSeconds(10));
    }

    @Test
    void recordMetrics() {
        assertThat(traceService.getStartupTimeline().getSteps().size()).isGreaterThan(0);
        new StartupRecorderTask(traceService.getStartupTimeline(), ofSeconds(10)).run();
    }

}