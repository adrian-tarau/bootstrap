package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.annotation.AnswerFor;
import net.microfalx.threadpool.ThreadPool;

@SuppressWarnings("unused")
@AnswerFor(ThreadPool.class)
public class ThreadPoolAnswer extends ExecutorAnswer {

}
