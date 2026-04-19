package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.annotation.AnswerFor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("unused")
@AnswerFor({Executor.class, ExecutorService.class, ScheduledExecutorService.class})
public class ExecutorAnswer extends AbstractAnswer {
}
