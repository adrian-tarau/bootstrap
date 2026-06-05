package net.microfalx.bootstrap.cli.command;

import net.microfalx.lang.ExceptionUtils;

import java.io.IOException;

/**
 * A command implemented by a runnable.
 */
public abstract class RunnableCommand extends Command implements Runnable {

    @Override
    public final void run() {
        try {
            execute();
        } catch (IOException e) {
            ExceptionUtils.rethrowException(e);
        }
    }

    protected abstract void execute() throws IOException;
}
