package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.FinishReason;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.ai.api.TokenStream;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.ThreadUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ExceptionUtils.rethrowException;

/**
 * Base class for token streams.
 */
public abstract class AbstractTokenStream implements TokenStream {

    final AtomicBoolean completed = new AtomicBoolean(false);
    final StringBuilder builder = new StringBuilder();
    FinishReason finishReason = FinishReason.STOP;
    volatile Throwable throwable;
    volatile Message message;
    volatile Integer inputTokenCount;
    volatile Integer outputTokenCount;

    @Override
    public final Message getMessage() {
        waitForCompletion();
        if (message != null) {
            return message;
        } else {
            return MessageImpl.create(Message.Type.MODEL, builder.toString());
        }
    }

    @Override
    public final FinishReason getFinishReason() {
        return finishReason;
    }

    @Override
    public boolean isComplete() {
        return completed.get();
    }

    @Override
    public final int getInputTokenCount() {
        return (int) NumberUtils.toNumber(inputTokenCount, 0);
    }

    @Override
    public final int getOutputTokenCount() {
        return (int) NumberUtils.toNumber(outputTokenCount, 0);
    }

    protected final void raiseIfError() {
        if (throwable != null) rethrowException(throwable);
    }

    protected final void waitForCompletion() {
        while (!completed.get()) {
            ThreadUtils.sleepMillis(10);
        }
    }
}
