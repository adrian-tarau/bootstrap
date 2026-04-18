package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.annotation.AnswerFor;
import net.microfalx.threadpool.ThreadPool;

@AnswerFor(ThreadPool.class)
public class ThreadPoolAnswer extends ExecutorAnswer {

}
