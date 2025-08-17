package net.microfalx.bootstrap.ai.api;

import java.util.Iterator;

/**
 * An interface representing a stream of tokens, typically used in language model processing.
 */
public interface TokenStream extends Iterator<Token>, TokenUsage {

    /**
     * Returns the complete answered message associated with this stream.
     *
     * @return a non-null instance
     */
    Message getAnswerMessage();

    /**
     * Returns the thinking message associated with this stream.
     *
     * @return a non-null instance
     */
    Message getThinkingMessage();

    /**
     * Returns the finish reason for the stream.
     *
     * @return a non-null instance of {@link FinishReason}
     */
    FinishReason getFinishReason();

    /**
     * Returns whether the AI is currently thinking.
     *
     * @return @{code true} if the AI is thinking, {@code false} otherwise.
     */
    boolean isThinking();

    /**
     * Returns when the stream is complete.
     *
     * @return {@code true} if the stream has been fully consumed, {@code false} otherwise.
     */
    boolean isComplete();
}
