package net.microfalx.bootstrap.core.utils;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;

import javax.naming.NameNotFoundException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.NoSuchAttributeException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarException;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which represents a failure description in the application. It contains information about the type of failure and the associated exception (if any).
 * This class can be used to encapsulate failure information and provide a standardized way to handle failures across the application.
 */
@Slf4j
@Getter
@ToString
public final class Failure {

    private static final Map<Class<? extends Throwable>, Type> exceptionTypes = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Throwable>, Type> exceptionSubTypes = new ConcurrentHashMap<>();
    private static final Metrics METRICS = Metrics.of("Failure");
    private static final Metrics MISSING = METRICS.withGroup("Missing");
    private static final Metrics MAPPED = METRICS.withGroup("Mapped");

    /**
     * The type of failure, which categorizes the nature of the failure (e.g., connectivity issues, authentication failures, etc).
     */
    private final Type type;

    /**
     * The exception associated with the failure, which provides more details about the failure. This can be null if there is no specific exception associated with the failure.
     */
    private final Throwable throwable;

    /**
     * Returns the failure description by looking into the call stack. It tries to extract <code>Failure.Type</code> using custom
     *
     * @param throwable the exception to examine
     * @return a non-null instance
     */
    public static Failure of(Throwable throwable) {
        if (throwable == null) return new Failure(Type.INTERNAL_ERROR, null);
        return new Failure(getType(throwable), throwable);
    }

    private Failure(Type type, Throwable throwable) {
        this.type = type;
        this.throwable = throwable;
    }

    /**
     * Returns a friendly name of the root cause exception. If there is no exception, it returns "N/A".
     *
     * @return a non-null instance
     * @see ExceptionUtils#getRootCauseName(Throwable)
     */
    public String getRootCauseName() {
        if (throwable == null) return StringUtils.NA_STRING;
        return ExceptionUtils.getRootCauseName(throwable);
    }

    /**
     * Returns the root cause message of the associated exception. If there is no exception, it returns "N/A".
     *
     * @return a non-null instance
     * @see ExceptionUtils#getRootCauseMessage(Throwable)
     */
    public String getRootCauseMessage() {
        if (throwable == null) return StringUtils.NA_STRING;
        return ExceptionUtils.getRootCauseMessage(throwable);
    }

    /**
     * Returns the root cause description.
     * <p>
     * The root cause description has the root cause message and the type of failure in parentheses.
     *
     * @return the description
     */
    public String getRootCauseDescription() {
        if (throwable == null) return StringUtils.NA_STRING;
        return ExceptionUtils.getRootCauseDescription(throwable);
    }

    /**
     * Returns whether the failure or any of its causes is of the given type.
     *
     * @param exceptionClass the exception class
     * @return {@code true} if the exception or any of its causes is of the given type, {@code false} otherwise
     * @see ExceptionUtils#contains(Throwable, Class)
     */
    public boolean contains(Class<? extends Throwable> exceptionClass) {
        if (throwable == null) return false;
        return ExceptionUtils.contains(throwable, exceptionClass);
    }

    /**
     * Returns whether the failure or any of its causes is any of the given types.
     *
     * @param exceptionClasses the exception classes
     * @return {@code true} if the exception or any of its causes is of the given types, {@code false} otherwise
     * @see ExceptionUtils#contains(Throwable, Class)
     */
    @SafeVarargs
    public final boolean contains(Class<? extends Throwable>... exceptionClasses) {
        if (throwable == null) return false;
        for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
            if (ExceptionUtils.contains(throwable, exceptionClass)) return true;
        }
        return false;
    }

    /**
     * Registers a failure type associated with a specific exception.
     *
     * @param type           exception type
     * @param exceptionClass exception class
     */
    public static void registerType(Failure.Type type, Class<? extends Throwable> exceptionClass) {
        requireNonNull(type);
        requireNonNull(exceptionClass);
        LOGGER.debug("Register failure type {} for {}", type, ClassUtils.getName(exceptionClass));
        exceptionTypes.put(exceptionClass, type);
    }

    /**
     * Registers a failure type associated with all subclasses of a give exception.
     *
     * @param type           exception type
     * @param exceptionClass exception class
     */
    public static void registerSubType(Failure.Type type, Class<? extends Throwable> exceptionClass) {
        requireNonNull(type);
        requireNonNull(exceptionClass);
        LOGGER.debug("Register failure sub type {} for {}", type, ClassUtils.getName(exceptionClass));
        exceptionSubTypes.put(exceptionClass, type);
    }

    /**
     * Returns the failure type by looking into the call stack. It tries to extract <code>Failure.Type</code> using custom
     * exception type extractors, and then it falls back to per exception class exception type.
     *
     * @param throwable an exception
     * @return the failure description
     */
    public static Failure.Type getType(Throwable throwable) {
        if (throwable == null) return Failure.Type.INTERNAL_ERROR;
        List<Throwable> throwableList = org.apache.commons.lang3.exception.ExceptionUtils.getThrowableList(throwable);
        Collections.reverse(throwableList);
        for (Throwable chainedThrowable : throwableList) {
            Throwable unwrapThrowable = unwrapThrowable(chainedThrowable);
            Class<? extends Throwable> unwrapThrowableClass = unwrapThrowable.getClass();
            Failure.Type type = exceptionTypes.get(unwrapThrowableClass);
            if (type != null && type != Failure.Type.INTERNAL_ERROR) return registerMapped(type);
            type = resolveType(unwrapThrowable);
            if (type != null && type != Failure.Type.INTERNAL_ERROR) return registerMapped(type);
        }
        registerMissing(throwable);
        return Failure.Type.INTERNAL_ERROR;
    }

    /**
     * Returns the real exception by unwrapping a few known JVM wrappers.
     *
     * @param throwable the exception to examine
     * @return The root cause
     */
    public static Throwable unwrapThrowable(Throwable throwable) {
        if (throwable == null) return null;
        Throwable cause = throwable;
        int iterationCount = 50;
        while (iterationCount-- > 0) {
            if (cause instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) cause).getTargetException();
            } else if (cause instanceof UndeclaredThrowableException) {
                cause = ((UndeclaredThrowableException) cause).getUndeclaredThrowable();
            } else {
                return cause;
            }
        }
        return cause;
    }

    public static void reset() {
        exceptionTypes.clear();
        exceptionSubTypes.clear();
        registerFailureTypes();
    }

    private static Failure.Type resolveType(Throwable throwable) {
        for (Map.Entry<Class<? extends Throwable>, Type> entry : exceptionSubTypes.entrySet()) {
            if (ClassUtils.isSubClassOf(throwable.getClass(), entry.getKey())) return entry.getValue();
        }
        return null;
    }

    private static void registerMissing(Throwable throwable) {
        MISSING.count(ClassUtils.getName(throwable));
    }

    private static Failure.Type registerMapped(Failure.Type type) {
        MAPPED.count(type.name());
        return type;
    }

    private static void registerFailureTypes() {
        registerType(Type.CONNECTIVITY, java.net.ConnectException.class);
        registerType(Type.CONNECTIVITY, ClosedChannelException.class);
        registerType(Type.CONNECTIVITY, ClosedByInterruptException.class);
        registerType(Type.CONNECTIVITY, javax.naming.CommunicationException.class);
        registerType(Type.CONNECTIVITY, java.rmi.ConnectException.class);
        registerType(Type.CONNECTIVITY, java.net.NoRouteToHostException.class);
        registerType(Type.CONNECTIVITY, UnknownHostException.class);
        registerType(Type.CONNECTIVITY, BindException.class);
        registerType(Type.CONNECTIVITY, NoRouteToHostException.class);
        registerType(Type.CONNECTIVITY, UnknownServiceException.class);
        registerType(Type.CONNECTIVITY, PortUnreachableException.class);

        registerType(Type.TIMED_OUT, java.util.concurrent.TimeoutException.class);
        registerType(Type.TIMED_OUT, java.net.SocketTimeoutException.class);

        registerType(Type.NETWORK, InterruptedIOException.class);
        registerType(Type.NETWORK, SocketException.class);
        registerSubType(Type.NETWORK, IOException.class);

        registerType(Type.RESOURCE_NOT_FOUND, FileNotFoundException.class);
        registerType(Type.RESOURCE_NOT_FOUND, NoSuchFileException.class);
        registerType(Type.RESOURCE_NOT_FOUND, NoSuchAttributeException.class);
        registerType(Type.RESOURCE_NOT_FOUND, NameNotFoundException.class);

        registerType(Type.AUTHORIZATION, AccessDeniedException.class);
        registerType(Type.AUTHORIZATION, SecurityException.class);

        registerType(Type.CORRUPTED_DATA, DataFormatException.class);
        registerType(Type.CORRUPTED_DATA, ZipException.class);
        registerType(Type.CORRUPTED_DATA, JarException.class);
        registerType(Type.CORRUPTED_DATA, EOFException.class);

        registerType(Type.ILLEGAL_INPUT, URISyntaxException.class);
        registerType(Type.ILLEGAL_INPUT, IllegalArgumentException.class);
        registerType(Type.ILLEGAL_INPUT, IndexOutOfBoundsException.class);
        registerType(Type.ILLEGAL_INPUT, ArrayIndexOutOfBoundsException.class);

        registerType(Type.SERVICE_UNAVAILABLE, ServiceUnavailableException.class);

        registerType(Type.INTERNAL_ERROR, NullPointerException.class);
    }

    static {
        registerFailureTypes();
    }

    /**
     * An enum which defines the type of failure. This is used to categorize failures and provide more context about the nature
     * of the failure. The specific types of failures can be defined based on the application's requirements, such as validation
     * errors, authentication failures, authorization failures, etc.
     */
    public enum Type {

        /**
         * A category for network accessibility failures (a service cannot be reached, etc).
         */
        CONNECTIVITY(true, true),

        /**
         * A category for network-related failures that are not necessarily connectivity issues, such as I/O exceptions,
         * socket exceptions, etc.
         */
        NETWORK(true, true),

        /**
         * A category for network/service timeout failures (it can be reached, but it doesn't respond in a timely manner).
         */
        TIMED_OUT(true, true),

        /**
         * A category for authentication failures (wrong credentials, cannot perform authentication, etc).
         */
        AUTHENTICATION(false, false),

        /**
         * A category for authorization failures (access denied, not enough privileges, etc).
         */
        AUTHORIZATION(false, false),

        /**
         * A category for service availability.
         */
        SERVICE_UNAVAILABLE(true, true),

        /**
         * A category for system capacity limits (too many requests in a given amount of time). Intended for use with rate limiting schemes.
         */
        OVERLOAD(true, true),

        /**
         * A category for missing resources (file, etc).
         */
        RESOURCE_NOT_FOUND(false, false),

        /**
         * A category for temporary unavailability of an existing resource due to resource being busy (used by another process).
         */
        RESOURCE_BUSY(true, true),

        /**
         * A category for temporary unavailability of a resource due to unknown causes.
         */
        RESOURCE_UNAVAILABLE(true, true),

        /**
         * Data access exceptions that are considered transient - where a previously failed operation might be able to succeed when the operation
         * is retried without any intervention by application-level functionality.
         */
        TRANSIENT_DATA_ACCESS(true, true),

        /**
         * Data access exceptions that are considered non-transient - where a retry of the same operation would fail unless the cause of the Exception
         * is corrected.
         */
        NON_TRANSIENT_DATA_ACCESS(false, false),

        /**
         * A category for all data corruption failures (cannot open archives & other formats, etc).
         */
        CORRUPTED_DATA(false, false),

        /**
         * A category for failures due to storage unavailability (file system, database storage, memory storage, etc).
         */
        INSUFFICIENT_STORAGE(true, true),

        /**
         * A category for failures due to application misconfiguration.
         */
        CONFIGURATION(false, false),

        /**
         * A category for failures due to invalid input when performing an action. It is different from <code>CORRUPTED_DATA</code> because it doesn't
         * represent data processing, just performing an action on invalid inputs. Failures could be:
         * <ul>
         * <li>failure to create an URI/URL</li>
         * <li>failure to invoke a database procedure due to illegal arguments</li>
         * <li>failure to invoke services with appropriate arguments</li>
         * </ul>
         * This failure is not recoverable without manual intervention (correcting a configuration, patching the application, etc).
         */
        ILLEGAL_INPUT(false, false),

        /**
         * A category for failures due to an invalid output when performing an action. This failure is not recoverable
         * without manual intervention (correcting a configuration, patching the application, etc).
         */
        ILLEGAL_OUTPUT(false, false),

        /**
         * A category for failures due to conflict in the request, such as an edit conflict (version conflict) or a constraint violation.
         */
        CONFLICT(false, true),

        /**
         * A category for "failures" which should not considered failures and they should be treated as
         * "abort/cancel current operation" and report no failure.
         */
        ABORT(false, false),

        /**
         * A category for "failures" which should not considered failures and they should be treated as
         * "client asks for a reset" and retried.
         */
        RESET(true, true),

        /**
         * A category for any other types of failures (usually bugs).
         */
        INTERNAL_ERROR(false, false);

        private final boolean retriable;
        private final boolean _transient;

        Type(boolean _transient, boolean retriable) {
            this._transient = _transient;
            this.retriable = retriable;
            if (_transient && !retriable) {
                throw new IllegalArgumentException("A transient failure should be retriable, name " + name());
            }
        }

        /**
         * Returns if the exception type indicates a failure that should be retried (using a retry policy).
         *
         * @return {@code true} if the failure should be retried, {@code false} otherwise
         */
        public boolean isRetriable() {
            return retriable;
        }

        /**
         * Returns whether the exception type indicates a failure that is transient, meaning that it might be resolved
         * without any intervention by application-level functionality, and that a retry might be successful.
         *
         * @return {@code true} if the failure is transient, {@code false} otherwise
         */
        public boolean isTransient() {
            return _transient;
        }

        /**
         * Returns whether the exception type indicates a failure that is related to network issues, such as connectivity
         * problems, timeouts, or other network-related errors.
         *
         * @return {@code true} if the failure is related to network issues, {@code false} otherwise
         */
        public boolean isNetwork() {
            return this == CONNECTIVITY || this == TIMED_OUT;
        }

        /**
         * Returns whether the exception type indicates a failure that is related to security issues,
         * such as authentication or authorization failures.
         *
         * @return {@code true} if the failure is related to security issues, {@code false} otherwise
         */
        public boolean isSecurity() {
            return this == AUTHENTICATION || this == AUTHORIZATION;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Type.class.getSimpleName() + "[", "]")
                    .add("name=" + name())
                    .add("retriable=" + retriable)
                    .add("_transient=" + _transient)
                    .toString();
        }
    }
}
