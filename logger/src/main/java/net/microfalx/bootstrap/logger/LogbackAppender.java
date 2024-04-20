package net.microfalx.bootstrap.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import net.microfalx.bootstrap.core.utils.IdGenerator;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Hashing;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An appender which takes all the log events and publish them to an application logger storage.
 */
class LogbackAppender extends ch.qos.logback.core.AppenderBase<ILoggingEvent> {

    private final LoggerListener storage;

    LogbackAppender(LoggerListener storage) {
        requireNonNull(storage);
        this.storage = storage;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LoggerEvent.LoggerEventBuilder builder = LoggerEvent.builder();
        builder.id(IdGenerator.get().next()).name(eventObject.getLoggerName()).threadName(eventObject.getThreadName());
        builder.level(EnumUtils.fromName(LoggerEvent.Level.class, eventObject.getLevel().toString()));
        builder.timestamp(eventObject.getTimeStamp()).message(eventObject.getFormattedMessage())
                .sequenceNumber(eventObject.getSequenceNumber()).correlationId(buildCorrelationId(eventObject));
        IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
        if (throwableProxy != null) {
            builder.exceptionClassName(throwableProxy.getClassName());
            Throwable throwable = getThrowable(throwableProxy);
            if (throwable != null) {
                builder.exceptionClassName(ClassUtils.getName(throwable));
                builder.exceptionStackTrace(ExceptionUtils.getStackTrace(throwable));
            }
        }
        storage.onEvent(builder.build());
    }

    private String buildCorrelationId(ILoggingEvent eventObject) {
        Hashing hashing = Hashing.create();
        hashing.update(name);
        hashing.update(eventObject.getLevel().levelInt);
        IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
        if (throwableProxy != null) {
            hashing.update(throwableProxy.getClassName());
            for (StackTraceElementProxy stackTraceElementProxy : throwableProxy.getStackTraceElementProxyArray()) {
                StackTraceElement stackTraceElement = stackTraceElementProxy.getStackTraceElement();
                hashing.update(stackTraceElement.getClassName());
                hashing.update(stackTraceElement.getMethodName());
                hashing.update(stackTraceElement.getLineNumber());
            }
        } else {
            hashing.update(eventObject.getMessage());
        }
        return hashing.asString();
    }

    private Throwable getThrowable(IThrowableProxy throwableProxy) {
        if (throwableProxy instanceof ThrowableProxy throwableProxy1) {
            return throwableProxy1.getThrowable();
        } else {
            return null;
        }
    }
}
