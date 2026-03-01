package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.FinishReason;
import net.microfalx.bootstrap.ai.api.Message;
import net.microfalx.bootstrap.ai.api.TokenStream;
import net.microfalx.lang.NumberUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNullElseGet;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.ThreadUtils.sleepMillis;

/**
 * Base class for token streams.
 */
public abstract class AbstractTokenStream implements TokenStream {

    final AtomicBoolean completed = new AtomicBoolean(false);
    final AtomicBoolean thinking = new AtomicBoolean(false);
    final StringBuilder answerBuilder = new StringBuilder();
    final StringBuilder thinkingBuilder = new StringBuilder();
    FinishReason finishReason = FinishReason.STOP;
    volatile Throwable throwable;
    volatile Message answerMessage;
    volatile Message thinkingMessage;
    volatile Integer inputTokenCount;
    volatile Integer outputTokenCount;

    @Override
    public final Message getAnswerMessage() {
        waitForCompletion();
        return requireNonNullElseGet(answerMessage, () -> MessageImpl.create(Message.Type.MODEL, answerBuilder.toString()));
    }

    @Override
    public final Message getThinkingMessage() {
        return requireNonNullElseGet(thinkingMessage, () -> MessageImpl.create(Message.Type.MODEL, thinkingBuilder.toString()));
    }

    @Override
    public final FinishReason getFinishReason() {
        return finishReason;
    }

    @Override
    public final boolean isThinking() {
        return thinking.get();
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
            sleepMillis(10);
        }
    }
}
