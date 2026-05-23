package net.microfalx.bootstrap.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import net.microfalx.lang.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An appender which takes all the log events and publish them to an application logger storage.
 */
class RecorderAppender extends ch.qos.logback.core.AppenderBase<ILoggingEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecorderAppender.class);

    static final String INSTALLED_FLAG = "BOOTSTRAP_APPENDER";

    volatile LoggerListener storage;
    final Queue<LoggerEvent> pendingEvents = new LinkedBlockingQueue<>();

    /**
     * Initializes the internal appender for a given logger context.
     *
     * @param loggerContext the logger context
     */
    static void initialize(LoggerContext loggerContext) {
        try {
            if (loggerContext.getObject(INSTALLED_FLAG) != null) return;
            RecorderAppender appender = new RecorderAppender();
            appender.setContext(loggerContext);
            appender.setName("storage");
            appender.start();
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            logger.addAppender(appender);
            loggerContext.putProperty(INSTALLED_FLAG, INSTALLED_FLAG);
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to initialize logback appender");
        }
    }

    /**
     * Resets the state of the appender inside the logger context.
     *
     * @param loggerContext the logger context
     */
    static void release(LoggerContext loggerContext) {
        loggerContext.removeObject(INSTALLED_FLAG);
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
        LoggerEvent loggerEvent = builder.build();
        if (storage != null) {
            storage.onEvent(loggerEvent);
        } else {
            pendingEvents.add(loggerEvent);
        }
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
