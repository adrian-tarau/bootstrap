package net.microfalx.bootstrap.cli.command;

import net.microfalx.lang.ExceptionUtils;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * A command implemented by a callable.
 */
public abstract class CallableCommand<T> extends Command implements Callable<T> {

    @Override
    public final T call() throws Exception {
        try {
            return execute();
        } catch (IOException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    protected abstract T execute() throws Exception;
}
