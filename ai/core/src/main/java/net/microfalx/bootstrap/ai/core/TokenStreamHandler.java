package net.microfalx.bootstrap.ai.core;

import net.microfalx.bootstrap.ai.api.FinishReason;
import net.microfalx.bootstrap.ai.api.Token;
import net.microfalx.bootstrap.core.utils.Failure;
import net.microfalx.lang.StringUtils;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Handles a token stream from a chat session, allowing iteration over the tokens.
 */
class TokenStreamHandler extends AbstractTokenStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenStreamHandler.class);

    private final AbstractChat chat;
    private final AiProperties properties;
    private final AiServiceImpl service;
    private final Iterator<Token> tokens;

    private final AtomicBoolean firstSeen = new AtomicBoolean(false);
    private final long startedNanos = System.nanoTime();
    private volatile Duration timeToFirstToken;
    private volatile FinishReason finishReason = FinishReason.OTHER;

    TokenStreamHandler(AiServiceImpl service, AbstractChat chat, Flux<ChatResponse> tokenStream) {
        requireNonNull(service);
        requireNonNull(chat);
        requireNonNull(tokenStream);
        this.service = service;
        this.properties = service.getProperties();
        this.chat = chat;
        this.tokens = initTokens(tokenStream);
        this.thinking.set(chat.getModel().isThinking());
    }

    @Override
    public boolean hasNext() {
        raiseIfError();
        return tokens.hasNext();
    }

    @Override
    public Token next() {
        return tokens.next();
    }

    @Override
    public boolean isComplete() {
        return !hasNext() && super.isComplete();
    }

    private boolean isTransient(Throwable ex) {
        return Failure.getType(ex).isTransient();
    }

    private void onNext(ChatResponse response) {
        if (firstSeen.compareAndSet(false, true)) {
            timeToFirstToken = Duration.ofNanos(System.nanoTime() - startedNanos);
            thinking.set(false);
        }
        ChatResponseMetadata metadata = response.getMetadata();
        Usage usage = metadata.getUsage();
        this.inputTokenCount = usage.getPromptTokens();
        this.outputTokenCount = usage.getCompletionTokens();
        if (metadata instanceof ChatGenerationMetadata chatMetadata) {
            finishReason = toFinishReason(chatMetadata.getFinishReason());
        }
    }

    private void onSubscribe(Subscription subscription) {
        thinking.set(true);
    }

    private void onComplete() {
        chat.streamCompleted(this);
        completed.set(true);
    }

    private void onError(Throwable ex) {
        this.throwable = ex;
        this.completed.set(true);
    }

    private void onFinally(SignalType signalType) {
        chat.streamCompleted(this);
        completed.set(true);
    }

    private Token mapToken(ChatResponse response) {
        return Token.create(Token.Type.ANSWER, response.getResult().getOutput().getText());
    }

    private boolean isNotEmpty(ChatResponse response) {
        return StringUtils.isNotEmpty(response.getResult().getOutput().getText());
    }

    private Iterator<Token> initTokens(Flux<ChatResponse> tokenStream) {
        return tokenStream
                .doOnSubscribe(this::onSubscribe)
                .doOnNext(this::onNext)
                .doOnComplete(this::onComplete)
                .doOnError(this::onError)
                .filter(this::isNotEmpty)
                .map(this::mapToken)
                .timeout(this.properties.getChatRequestTimeout())
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)).jitter(0.2).filter(this::isTransient))
                .doFinally(this::onFinally).toStream().iterator();

    }

    private FinishReason toFinishReason(String finishReason) {
        if (finishReason == null) return FinishReason.OTHER;
        return FinishReason.STOP;
    }
}
