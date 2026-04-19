package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.TaskTracker;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExecutorAnswerTest extends ServiceUnitTestCase {

    @Mock
    private Executor executor;

    @Mock
    private ExecutorService executorService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private TestRunnable runnable = new TestRunnable();
    private TestCallable callable = new TestCallable();

    private TaskTracker taskTracker;

    @Test
    void executor() {
        testAndAssertExecutor(executor);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
    }

    @Test
    void executorService() {
        testAndAssertExecutor(executorService);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
        testAndAssertExecutorService(executorService);
        assertEquals(1, taskTracker.getSubmitCount(TestRunnable.class));
        assertEquals(1, taskTracker.getSubmitCount(TestCallable.class));
    }

    @Test
    void scheduledExecutorService() {
        testAndAssertExecutor(scheduledExecutorService);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
        testAndAssertExecutorService(scheduledExecutorService);
        testAndAssertScheduledExecutorService(scheduledExecutorService);
        assertEquals(2, taskTracker.getScheduleCount(TestRunnable.class));
        assertEquals(2, taskTracker.getScheduleCount(TestCallable.class));
    }

    private void testAndAssertExecutor(Executor executor) {
        assertNotNull(executor);
        executor.execute(runnable);
        assertEquals(0, runnable.counter);
    }

    private void testAndAssertExecutorService(ExecutorService executorService) {
        assertNotNull(executorService);
        executorService.execute(runnable);
        assertEquals(0, runnable.counter);

        executorService.submit(runnable);
        assertEquals(0, runnable.counter);

        executorService.submit(callable);
        assertEquals(0, callable.counter);
    }

    private void testAndAssertScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        assertNotNull(scheduledExecutorService);

        scheduledExecutorService.schedule(runnable, 100, TimeUnit.MILLISECONDS);
        assertEquals(0, runnable.counter);

        scheduledExecutorService.schedule(callable, 100, TimeUnit.MILLISECONDS);
        assertEquals(0, callable.counter);

        scheduledExecutorService.scheduleAtFixedRate(runnable, 100, 100, TimeUnit.MILLISECONDS);
        assertEquals(0, runnable.counter);

        scheduledExecutorService.scheduleWithFixedDelay(runnable, 100, 100, TimeUnit.MILLISECONDS);
        assertEquals(0, runnable.counter);
    }

    private final class TestRunnable implements Runnable {

        private int counter;

        @Override
        public void run() {
            counter++;
        }
    }

    private final class TestCallable implements Callable<String> {

        private int counter;

        @Override
        public String call() throws Exception {
            counter++;
            return "test";
        }
    }

}