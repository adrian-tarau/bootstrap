package net.microfalx.bootstrap.core.async;

import net.microfalx.lang.*;
import net.microfalx.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.formatMessage;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.TimeUtils.FIVE_MINUTE;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * A function which is executed asynchronously.
 * <p>
 * The function returns a cached value (based on time out) and it only blocks longer than the timeout when is called for the first time.
 * <p>
 * When a new value needs to be calculated, only one thread will invoke the original function to calculate the value and all waiting threads
 * will reuse the newly calculated value.
 * <p>
 * An input identifier is calculated (to cache function results) either by providing a function to {@link #withIdentifier(Function)} or the input is a subclass of {@link Identifiable}.
 * In the absence of an input identifier provider, a hash is created out of the input object. The input object is never stored/persisted between requests.
 * <p>
 * The timeout controls the maximum amount of time to wait syncronoulsy for a result and also to decide
 * when a value needs to be calculated again (because it is stale).
 */
public class AsynchronousFunction<I, O> implements Identifiable<String>, Cloneable, Function<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousFunction.class);

    private static final Metrics METRICS = Metrics.of("Asynchronous Function");

    private final String id;
    private final Function<I, O> function;
    private BiConsumer<I, Object> onBefore;

    private Supplier<O> defaultValueSupplier;
    private boolean useDefaultWithFailures;

    private Duration timeout = Duration.ofMillis(100);
    private Duration expiration = Duration.ofSeconds(5);
    private Function<I, String> identifierProvider = ObjectUtils::toIdentifier;
    private AsyncTaskExecutor taskExecutor;

    private final AtomicInteger requestCount = new AtomicInteger();
    private final AtomicInteger resolvedCount = new AtomicInteger();

    private static final Map<String, ResponseHolder<?>> responses = new ConcurrentHashMap<>();
    private static final ThreadLocal<Object> CONTEXT = new ThreadLocal<>();
    private static volatile long lastCleanup = System.currentTimeMillis();

    public static <I, O> AsynchronousFunction<I, O> create(String id, Function<I, O> function) {
        return new AsynchronousFunction<>(id, function);
    }

    private AsynchronousFunction(String id, Function<I, O> function) {
        requireNonNull(id);
        requireNonNull(function);

        this.id = toIdentifier(id);
        this.function = function;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Invokes the function is necessary, otherwise returns a cached value (the previous function result)
     * and triggers a new function call in a separate thread.
     * <p>
     * The implementation throws any exception raised during the asynchronous calculation of the value.
     *
     * @param input the function argument
     * @return the function result or a cached value
     */
    @Override
    public O apply(I input) {
        requireNonNull(input);
        requestCount.incrementAndGet();
        cleanup(false);
        ResponseHolder<O> responseHolder = getResponse(input);
        O result = responseHolder.getResult();
        if (!isExpired(responseHolder) && result != null) {
            CONTEXT.remove();
            return result;
        } else {
            return execute(input, responseHolder);
        }
    }

    /**
     * Attaches a context to the function chain. The context will be available in onBefore event.
     *
     * @param context the context
     * @return self
     */
    public AsynchronousFunction<I, O> setContext(Object context) {
        CONTEXT.set(context);
        return this;
    }

    /**
     * Returns whether the function has a result cached available, expired or not.
     *
     * @param input the input
     * @return <code>true</code> if a result is available to be returned without blocking, <code>false</code> otherwise
     */
    public boolean hasResult(I input) {
        requireNonNull(input);
        ResponseHolder<O> responseHolder = getResponse(input);
        return !responseHolder.firstRequest.get();
    }

    /**
     * Returns whether the function has a result cached and it did not expired.
     *
     * @param input the input
     * @return <code>true</code> if a result is available but expired, <code>false</code> otherwise
     */
    public boolean isExpired(I input) {
        requireNonNull(input);
        ResponseHolder<O> responseHolder = getResponse(input);
        return responseHolder.firstRequest.get() || isExpired(responseHolder);
    }

    /**
     * Returns the result associated with the input.
     * <p>
     * The method never blocks and returns the available result (if any, use {@link #hasResult(Object)} or {@link #isExpired(Object)} to know
     * if the result is good).
     *
     * @param input the input
     * @return the result
     */
    public O getResult(I input) {
        requireNonNull(input);
        ResponseHolder<O> responseHolder = getResponse(input);
        return responseHolder.getResult();
    }

    /**
     * Returns the number of times the asynchronous function was called.
     *
     * @return a positive integer
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    /**
     * Returns the number of times the function was called to resolve a value.
     *
     * @return a positive integer
     */
    public int getResolvedCount() {
        return resolvedCount.get();
    }

    /**
     * Creates a new instance of the function, with a different thread pool.
     *
     * @param taskExecutor the task executor
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withThreadPool(AsyncTaskExecutor taskExecutor) {
        requireNonNull(taskExecutor);
        AsynchronousFunction<I, O> copy = copy();
        copy.taskExecutor = taskExecutor;
        return copy;
    }

    /**
     * Creates a new instance of the function, with a different indeitifier provider.
     *
     * @param identifierProvider a function to calculate a unique identifier for an input.
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withIdentifier(Function<I, String> identifierProvider) {
        AsynchronousFunction<I, O> copy = copy();
        copy.identifierProvider = identifierProvider != null ? identifierProvider : ObjectUtils::toIdentifier;
        return copy;
    }

    /**
     * Creates a new instance of the function, with a default in case of the function fails.
     *
     * @param defaultValue the default value in case of a timeout
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withDefaultValue(Supplier<O> defaultValue) {
        return withDefaultValue(defaultValue, true);
    }

    /**
     * Creates a new instance of the function, with a default in case of the function fails.
     * <p>
     * A timeout or a network failure is not considered a failure.
     *
     * @param defaultValue           the default value in case of a timeout
     * @param useDefaultWithFailures <code>true</code> to return a default even when an exception happens (in addition to time outs),
     *                               <code>false</code> othereise
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withDefaultValue(Supplier<O> defaultValue, boolean useDefaultWithFailures) {
        requireNonNull(defaultValue);
        AsynchronousFunction<I, O> copy = copy();
        copy.defaultValueSupplier = defaultValue;
        copy.useDefaultWithFailures = useDefaultWithFailures;
        return copy;
    }

    /**
     * Creates a new instance of the function, with a different timeout.
     *
     * @param timeout the timeout
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withTimeout(Duration timeout) {
        requireNonNull(timeout);
        AsynchronousFunction<I, O> copy = copy();
        copy.timeout = timeout;
        if (copy.timeout.toMillis() < 0) throw new IllegalArgumentException("Timeout must be a positive integer");
        return copy;
    }

    /**
     * Creates a new instance of the function, with a different expiration.
     *
     * @param expiration the timeout
     * @return a new instance
     */
    public AsynchronousFunction<I, O> withExpiration(Duration expiration) {
        requireNonNull(expiration);
        AsynchronousFunction<I, O> copy = copy();
        copy.expiration = expiration;
        if (copy.expiration.toMillis() < 0) throw new IllegalArgumentException("Expiration must be a positive integer");
        return copy;
    }

    /**
     * Creates a new instance of the function, with a different before event.
     * <p>
     * The before event handler is invoked before the function is called to prepare the execution context.
     *
     * @param before the callback
     * @return a new instance
     */
    public <C> AsynchronousFunction<I, O> withOnBefore(BiConsumer<I, C> before) {
        requireNonNull(before);
        AsynchronousFunction<I, O> copy = copy();
        copy.onBefore = (BiConsumer<I, Object>) before;
        return copy;
    }

    /**
     * Executes an asynchronous worker under a lock to invoke the function and get a result.
     * <p>
     * The function checks first, after it ackuires the lock if a result was already calculated and returns that result.
     * <p>
     * The function makes sure only one thread performs the calculation. If the function is triggered, the caller will wait
     * a maximum of {@link #timeout} millis before returning a cached value.
     *
     * @param input          the input of the function
     * @param responseHolder the response structure
     * @return the response of the function
     */
    private O execute(I input, ResponseHolder<O> responseHolder) {
        responseHolder.lock.lock();
        try {
            O result = responseHolder.getResult();
            if (!isExpired(responseHolder) && result != null) return result;
            boolean firstRequest = responseHolder.firstRequest.get();
            if (firstRequest || responseHolder.pending.compareAndSet(false, true)) {
                try {
                    Future<O> future = getTaskExecutor().submit(new RequestCallable(input, CONTEXT.get(), responseHolder, function));
                    if (firstRequest) {
                        O newResult = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        resolvedCount.incrementAndGet();
                        return responseHolder.updateResult(newResult);
                    } else {
                        return result;
                    }
                } catch (InterruptedException e) {
                    return ExceptionUtils.rethrowExceptionAndReturn(e);
                } catch (Exception e) {
                    return handleTimeout(e, responseHolder, input, result);
                }
            } else {
                return result;
            }
        } finally {
            CONTEXT.remove();
            responseHolder.lock.unlock();
        }
    }

    /**
     * Runs maintenance tasks to cleanup stale responses
     */
    public static void cleanup(boolean force) {
        if (!force && millisSince(lastCleanup) < FIVE_MINUTE) return;
        for (Map.Entry<String, ResponseHolder<?>> entry : responses.entrySet()) {
            if (millisSince(entry.getValue().lastUpdate) > FIVE_MINUTE) {
                METRICS.count("Cleanup");
                responses.remove(entry.getKey());
            }
        }
        lastCleanup = System.currentTimeMillis();
    }

    /**
     * Removes all cached responses.
     */
    public static void clear() {
        METRICS.count("Clear", responses.size());
        responses.clear();
    }

    private O handleTimeout(Throwable throwable, ResponseHolder<O> responseHolder, I input, O result) {
        O defaultResult = getDefaultResult(input, responseHolder);
        if (defaultValueSupplier != null) {
            if (useDefaultWithFailures) {
                return defaultResult;
            } else if (isTimeoutAndNetwork(throwable)) {
                return defaultResult;
            }
        }
        return rethrowExceptionAndReturn(throwable.getCause() != null ? throwable.getCause() : throwable);
    }

    private boolean isTimeoutAndNetwork(Throwable throwable) {
        return throwable instanceof TimeoutException || throwable instanceof ConnectException;
    }

    private O getDefaultResult(I input, ResponseHolder<O> responseHolder) {
        O defaultResult = responseHolder.getDefaultResult();
        if (defaultResult == null && defaultValueSupplier != null) {
            defaultResult = getDefaultWithTimer(input);
            responseHolder.updateDefaultResult(defaultResult);
        }
        return defaultResult;
    }

    private O getDefaultWithTimer(I input) {
        return METRICS.time("Default", (Supplier<O>) () -> defaultValueSupplier.get());
    }

    private AsyncTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) {
            taskExecutor = TaskExecutorFactory.create("async_function").createExecutor();
        }
        return taskExecutor;
    }

    private String getRequestId(I input) {
        invokeOnBefore(input);
        return this.id + "_" + getInputId(input);
    }

    private String getInputId(I input) {
        return identifierProvider.apply(input);
    }

    @SuppressWarnings("unchecked")
    private ResponseHolder<O> getResponse(I input) {
        return (ResponseHolder<O>) responses.computeIfAbsent(getRequestId(input), s -> new ResponseHolder<>());
    }

    private boolean isExpired(ResponseHolder<O> response) {
        return millisSince(response.lastUpdate) > (expiration.toMillis() - timeout.toMillis());
    }

    private void invokeOnBefore(I input) {
        invokeOnBefore(input, CONTEXT.get());
    }

    private void invokeOnBefore(I input, Object context) {
        if (onBefore != null) METRICS.time("Before", (t) -> onBefore.accept(input, context));
    }

    @SuppressWarnings("unchecked")
    private AsynchronousFunction<I, O> copy() {
        try {
            return (AsynchronousFunction<I, O>) clone();
        } catch (CloneNotSupportedException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    private String getMonitorName(I input) {
        return StringUtils.capitalizeWords(id) + " [ " + getInputId(input) + "]";
    }

    class RequestCallable implements Callable<O> {

        private final I input;

        private final ResponseHolder<O> response;
        private final Object context;
        private final Function<I, O> function;

        RequestCallable(I input, Object context, ResponseHolder<O> response, Function<I, O> function) {
            this.input = input;
            this.context = context;
            this.response = response;
            this.function = function;
        }

        @Override
        public O call() throws Exception {
            try {
                invokeOnBefore(input, context);
                O result = METRICS.time("Execute", (Supplier<O>) () -> function.apply(input));
                response.updateResult(result);
                response.firstRequest.set(false);
                return result;
            } catch (Exception e) {
                getDefaultResult(input, response);
                if (isTimeoutAndNetwork(e)) {
                    METRICS.count(getMonitorName(input));
                    LOGGER.warn(StringUtils.formatMessage("Function ''{0}'' timed out for input ''{1}''", id, getInputId(input)), e);
                } else if (useDefaultWithFailures) {
                    METRICS.increment("Failure");
                    LOGGER.warn(formatMessage("Function ''{0}'' failed out for input ''{1}''", id, getInputId(input)), e);
                }
                throw e;
            } finally {
                response.pending.set(false);
            }
        }
    }

    static class ResponseHolder<RO> {

        private final Lock lock = new ReentrantLock();
        private volatile long lastUpdate = TimeUtils.oneHourAgo();

        private volatile SoftReference<RO> result;
        private volatile SoftReference<RO> defaultResult;

        private final AtomicBoolean firstRequest = new AtomicBoolean(true);
        private final AtomicBoolean pending = new AtomicBoolean();

        RO getResult() {
            return result != null ? result.get() : null;
        }

        RO getDefaultResult() {
            return defaultResult != null ? defaultResult.get() : null;
        }

        RO updateResult(RO result) {
            this.result = new SoftReference<>(result);
            this.lastUpdate = System.currentTimeMillis();
            return result;
        }

        RO updateDefaultResult(RO result) {
            this.defaultResult = new SoftReference<>(result);
            this.lastUpdate = System.currentTimeMillis();
            return result;
        }

        @Override
        public String toString() {
            return "ResponseHolder{" +
                    "lock=" + lock +
                    ", lastUpdate=" + lastUpdate +
                    ", result=" + result +
                    ", defaultResult=" + defaultResult +
                    ", firstRequest=" + firstRequest +
                    ", pending=" + pending +
                    '}';
        }
    }
}
