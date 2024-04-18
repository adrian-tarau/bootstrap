package net.microfalx.bootstrap.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import net.microfalx.bootstrap.core.utils.IdGenerator;
import net.microfalx.lang.EnumUtils;

import java.util.Arrays;
import java.util.List;

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
                .sequenceNumber(eventObject.getSequenceNumber());
        IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
        if (throwableProxy != null) {
            builder.exceptionClassName(throwableProxy.getClassName());
            List<StackTraceElement> stackTraceElements = Arrays.stream(throwableProxy.getStackTraceElementProxyArray())
                    .map(StackTraceElementProxy::getStackTraceElement).toList();
            builder.stackTraceElements(stackTraceElements.toArray(new StackTraceElement[0]));
        }
        storage.onEvent(builder.build());
    }
}
