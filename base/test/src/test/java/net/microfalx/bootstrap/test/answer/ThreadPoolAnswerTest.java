package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.TaskTracker;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThreadPoolAnswerTest extends ServiceUnitTestCase {

    @Mock
    private ThreadPool threadPool;

    private final TestRunnable runnable = new TestRunnable();
    private final TestCallable callable = new TestCallable();

    private TaskTracker taskTracker;

    @Test
    void executor() {
        testAndAssertExecutor(threadPool);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
    }

    @Test
    void executorService() {
        testAndAssertExecutor(threadPool);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
        testAndAssertExecutorService(threadPool);
        assertEquals(1, taskTracker.getSubmitCount(TestRunnable.class));
        assertEquals(1, taskTracker.getSubmitCount(TestCallable.class));
    }

    @Test
    void scheduledExecutorService() {
        testAndAssertExecutor(threadPool);
        assertEquals(1, taskTracker.getExecutionCount(TestRunnable.class));
        testAndAssertExecutorService(threadPool);
        testAndAssertScheduledExecutorService(threadPool);
        assertEquals(1, taskTracker.getScheduleCount(TestRunnable.class));
        assertEquals(1, taskTracker.getScheduleCount(TestCallable.class));
    }

    @Test
    void stats() {
        assertNotNull(threadPool.getMetrics());
        assertEquals(0, threadPool.getMetrics().getExecutedTaskCount());
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

    private static final class TestRunnable implements Runnable {

        private int counter;

        @Override
        public void run() {
            counter++;
        }
    }

    private static final class TestCallable implements Callable<String> {

        private int counter;

        @Override
        public String call() throws Exception {
            counter++;
            return "test";
        }
    }

}