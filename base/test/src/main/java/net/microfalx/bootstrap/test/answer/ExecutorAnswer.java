package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.TaskTracker;
import net.microfalx.bootstrap.test.annotation.AnswerFor;
import org.mockito.invocation.InvocationOnMock;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("unused")
@AnswerFor({Executor.class, ExecutorService.class, ScheduledExecutorService.class})
public class ExecutorAnswer extends AbstractAnswer {

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        return switch (methodName) {
            case "execute" -> {
                Runnable runnable = invocation.getArgument(0);
                getTaskTracker().registerExecution(runnable);
                yield null;
            }
            case "submit" -> {
                Object task = invocation.getArgument(0);
                yield getTaskTracker().registerSubmit(task);
            }
            case "schedule" -> {
                Object task = invocation.getArgument(0);
                yield getTaskTracker().registerSchedule(task);
            }
            default -> super.answer(invocation);
        };
    }

    protected final TaskTracker getTaskTracker() {
        return getContext().getTaskTracker();
    }


}
