package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.annotation.AnswerFor;
import net.microfalx.lang.TimeUtils;
import net.microfalx.threadpool.ThreadPool;
import org.mockito.invocation.InvocationOnMock;

import java.time.ZonedDateTime;

import static java.lang.System.currentTimeMillis;

@SuppressWarnings("unused")
@AnswerFor(ThreadPool.class)
public class ThreadPoolAnswer extends ExecutorAnswer {

    private final ThreadPool.Metrics metrics = new MetricsImpl();

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        if ("getMetrics".equals(methodName)) {
            return metrics;
        } else {
            return super.answer(invocation);
        }
    }

    private class MetricsImpl implements ThreadPool.Metrics {

        private static final long created = currentTimeMillis();

        @Override
        public ZonedDateTime getCreatedTime() {
            return TimeUtils.toZonedDateTime(created);
        }

        @Override
        public int getRunningTaskCount() {
            return 0;
        }

        @Override
        public int getPendingTaskCount() {
            return 0;
        }

        @Override
        public int getFailedTaskCount() {
            return 0;
        }

        @Override
        public long getExecutedTaskCount() {
            return getTaskTracker().getExecutionCounts();
        }

        @Override
        public int getAvailableThreadCount() {
            return 0;
        }

        @Override
        public int getCreatedThreadCount() {
            return 0;
        }

        @Override
        public int getDestroyedThreadCount() {
            return 0;
        }
    }
}
