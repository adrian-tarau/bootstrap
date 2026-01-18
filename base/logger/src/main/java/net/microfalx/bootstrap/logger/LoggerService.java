package net.microfalx.bootstrap.logger;

import biz.paluch.logging.gelf.logback.GelfLogbackAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.store.Query;
import net.microfalx.bootstrap.store.Store;
import net.microfalx.bootstrap.store.StoreService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.logger.LoggerUtils.METRICS_COUNTS_EXCEPTION;
import static net.microfalx.bootstrap.logger.LoggerUtils.METRICS_COUNTS_SEVERITY;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.defaultIfNull;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggerService extends ApplicationContextSupport implements InitializingBean, LoggerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerService.class);

    @Autowired private ApplicationContext applicationContext;
    @Autowired private StoreService storeService;
    @Autowired private LoggerProperties properties;
    @Autowired private ThreadPool threadPool;

    private String hostname;

    private Store<LoggerEvent, Long> store;

    private Store<AlertEvent, String> alertStore;

    private final Collection<LoggerListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, AlertEvent> alerts = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        initHostInformation();
        initializeListeners();
        initializeStores();
        initializeAppenders();
        initializeWorkers();
    }

    /**
     * Returns the application alerts for a given time interval.
     *
     * @param start the start time
     * @param end   the end time
     * @return a non-null instance
     */
    public Collection<AlertEvent> getAlerts(LocalDateTime start, LocalDateTime end) {
        return alertStore.list(Query.<AlertEvent>builder().start(start).end(end).build());
    }

    /**
     * Returns an alert by its identifier.
     *
     * @param id the alert identifier
     * @return the alert, null if it does not exist
     */
    public AlertEvent getAlert(String id) {
        requireNonNull(id);
        return alertStore.find(id);
    }

    /**
     * Registers a logger listener.
     *
     * @param loggerListener the listener
     */
    public void registerLoggerListener(LoggerListener loggerListener) {
        requireNonNull(loggerListener);
        if (!(loggerListener instanceof LoggerService)) {
            LOGGER.info("Logger listener '{}'", ClassUtils.getName(loggerListener));
            if (loggerListener instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(this.applicationContext);
            }
            listeners.add(loggerListener);
        }
    }

    /**
     * Clears all alerts.
     */
    public long clearAlerts() {
        long count = alertStore.clear();
        alerts.clear();
        return count;
    }

    /**
     * Acknowledge pending alerts.
     */
    public int acknowledgeAlerts() {
        AtomicInteger count = new AtomicInteger(0);
        Query<AlertEvent> query = Query.<AlertEvent>builder().start(LocalDateTime.now().minusDays(7)).build();
        alertStore.update(query, event -> {
            event.setAcknowledged(true);
            event.setPendingEventCount(0);
            count.incrementAndGet();
            return true;
        });
        alerts.clear();
        return count.get();
    }

    private void initializeListeners() {
        ClassUtils.resolveProviderInstances(LoggerListener.class).forEach(this::registerLoggerListener);
        getBeansOfType(LoggerListener.class).forEach(this::registerLoggerListener);
    }

    private void initHostInformation() {
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
    }

    private void initializeStores() {
        Store.Options options = Store.Options.create(LoggerUtils.LOGGER_STORE, "Logger");
        store = storeService.registerStore(options);
        options = Store.Options.create(LoggerUtils.ALERT_STORE, "Alert");
        alertStore = storeService.registerStore(options);
    }

    private void initializeAppenders() {
        initializeApplicationAppender();
        initializeGelfAppender();
        initializeSyslogAppender();
    }

    private void initializeApplicationAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            Appender<ILoggingEvent> appender = appenderIterator.next();
            if (appender instanceof LogbackAppender internalAppender) {
                processQueuedLoggerEvents(internalAppender.pending);
                internalAppender.storage = this;
            }
        }
    }

    private void initializeGelfAppender() {
        LoggerProperties.Gelf gelf = properties.getGelf();
        if (StringUtils.isEmpty(gelf.getHostname())) return;
        LOGGER.info("Send logs using GELF to " + gelf.toUri());
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        GelfLogbackAppender appender = new GelfLogbackAppender();
        appender.setHost(gelf.getHostname());
        appender.setPort(gelf.getPort());
        appender.setIncludeFullMdc(true);
        appender.setIncludeLocation(true);
        appender.setExtractStackTrace("true");
        appender.setOriginHost(hostname);
        appender.setFacility(gelf.getFacility());
        appender.setAdditionalFields("Application=" + defaultIfNull(properties.getApplication(), "Bootstrap"));
        appender.setAdditionalFields("Process=" + defaultIfNull(properties.getProcess(), "Web"));
        appender.setContext(logCtx);
        appender.setName("gelf");
        appender.start();
        ch.qos.logback.classic.Logger logger = logCtx.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    private void initializeSyslogAppender() {
        LoggerProperties.Syslog syslog = properties.getSyslog();
        if (StringUtils.isEmpty(syslog.getHostname())) return;
        LOGGER.info("Send logs using Syslog to " + syslog.toUri());
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        SyslogAppender appender = new SyslogAppender();
        appender.setSyslogHost(syslog.getHostname());
        appender.setPort(syslog.getPort());
        appender.setFacility(syslog.getFacility());
        appender.setThrowableExcluded(true);
        appender.setContext(logCtx);
        appender.setName("syslog");
        appender.start();
        ch.qos.logback.classic.Logger logger = logCtx.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    private void initializeWorkers() {
        threadPool.submit(new AcknowledgeAlertsTask());
    }

    @Override
    public void onEvent(LoggerEvent event) {
        requireNonNull(event);
        try {
            trackLogEvents(event);
            processLogEvent(event);
            processAlertEvent(event);
            forwardLogEvent(event);
        } catch (Throwable e) {
            LoggerUtils.METRICS_FAILURE.increment(ExceptionUtils.getRootCauseName(e));
        }
    }

    private void trackLogEvents(LoggerEvent event) {
        METRICS_COUNTS_SEVERITY.count(event.getLevel().name());
        if (event.getExceptionClassName() != null) METRICS_COUNTS_EXCEPTION.count(event.getExceptionClassName());
    }

    private void processQueuedLoggerEvents(Queue<LoggerEvent> events) {
        for (; ; ) {
            LoggerEvent event = events.poll();
            if (event == null) break;
            onEvent(event);
        }
    }

    private void forwardLogEvent(LoggerEvent event) {
        for (LoggerListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable e) {
                String listenerClassName = ClassUtils.getName(listener);
                LOGGER.debug("Failed to forward logging event to '{}', event {}", listenerClassName, event);
                LoggerUtils.METRICS_FORWARD_FAILURE.increment(listenerClassName);
            }
        }
    }

    private void processLogEvent(LoggerEvent event) {
        try {
            store.add(event);
        } catch (Throwable e) {
            LOGGER.debug("Failed to store logging event '{}' to internal storage", event);
            LoggerUtils.METRICS_EVENT_STORE_FAILURE.increment(ExceptionUtils.getRootCauseName(e));
        }
    }

    private void processAlertEvent(LoggerEvent event) {
        if (!event.getLevel().isHigherSeverity(LoggerEvent.Level.WARN)) return;
        AlertEvent alert = getAlert(event);
        alert.update(event);
        storeAlert(alert);
    }

    private void storeAlert(AlertEvent event) {
        try {
            alertStore.add(event);
        } catch (Throwable e) {
            LOGGER.debug("Failed to store logging event '{}' to internal storage", event);
            LoggerUtils.METRICS_ALERT_STORE_FAILURE.increment(ExceptionUtils.getRootCauseName(e));
        }
    }

    private AlertEvent getAlert(LoggerEvent event) {
        return alerts.computeIfAbsent(event.getCorrelationId(), s -> {
            AlertEvent alertEvent = alertStore.find(s);
            if (alertEvent == null) alertEvent = AlertEvent.builder().id(s).build();
            return alertEvent;
        });
    }

    class AcknowledgeAlertsTask implements Runnable {

        @Override
        public void run() {
            acknowledgeAlerts();
        }
    }

}
