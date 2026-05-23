package net.microfalx.bootstrap.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.File;

import static net.microfalx.bootstrap.logger.LoggerUtils.getLoggerContext;
import static net.microfalx.bootstrap.logger.LoggerUtils.getRootLogger;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatBytes;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A collection of appenders used to log everything to files.
 */
class ApplicationAppenders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAppenders.class);

    private final Environment environment;
    private File logsDirectory;

    public ApplicationAppenders(Environment environment) {
        requireNonNull(environment);
        this.environment = environment;
    }

    void register() {
        try {
            if (hasLogsDirectory()) {
                addStandardAppenders();
                addRegisteredAppenders();
            }
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Error registering logger appenders");
        }
    }

    private void addRegisteredAppenders() {
        LoggerLoader loader = new LoggerLoader();
        loader.load();
        LOGGER.debug("Loaded {}", loader.getAppenders().size() + " from descriptors");
        LoggerContext loggerContext = getLoggerContext();
        for (net.microfalx.bootstrap.logger.Appender appender : loader.getAppenders()) {
            FileAppender<ILoggingEvent> fileAppender = createAppender(appender, loggerContext);
            for (String included : appender.getIncluded()) {
                loggerContext.getLogger(included).addAppender(fileAppender);
            }
        }
    }

    private void addStandardAppenders() {
        // log everything in one file, easier to track
        registerAllAppender();
        // log all warnings in one file
        net.microfalx.bootstrap.logger.Appender warnAppender = net.microfalx.bootstrap.logger.Appender.builder("process.warn").build();
        registerAppender(warnAppender, Level.WARN);
        // log all errors in one file
        net.microfalx.bootstrap.logger.Appender errorAppender = net.microfalx.bootstrap.logger.Appender.builder("process.error").build();
        registerAppender(errorAppender, Level.ERROR);
    }

    private void registerAllAppender() {
        LoggerContext loggerContext = getLoggerContext();
        net.microfalx.bootstrap.logger.Appender allAppender = net.microfalx.bootstrap.logger.Appender.builder("process").build();
        FileAppender<ILoggingEvent> allFileAppender = createAppender(allAppender, loggerContext);
        getRootLogger().addAppender(allFileAppender);
    }

    private void registerAppender(net.microfalx.bootstrap.logger.Appender appender, Level level) {
        LoggerContext loggerContext = getLoggerContext();
        ch.qos.logback.classic.Logger logger = getRootLogger();
        FileAppender<ILoggingEvent> warnFileAppender = createAppender(appender, loggerContext);
        LevelFilter filter = new LevelFilter();
        filter.setLevel(level);
        filter.setOnMatch(FilterReply.ACCEPT);
        filter.setOnMismatch(FilterReply.DENY);
        filter.start();
        warnFileAppender.addFilter(filter);
        logger.addAppender(warnFileAppender);
    }

    private FileAppender<ILoggingEvent> createAppender(net.microfalx.bootstrap.logger.Appender appender, LoggerContext context) {
        PatternLayoutEncoder layoutEncoder = createLayoutEncoder(context);

        FixedWindowRollingPolicy rollingPolicy = createRollingPolicy(appender, context);
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = createTriggeringPolicy(context);

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setImmediateFlush(true);
        fileAppender.setBufferSize(FileSize.valueOf("8KB"));
        fileAppender.setFile(new File(getLogsDirectory(), appender.getFileName()).getAbsolutePath());
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        fileAppender.setName(appender.getName());
        fileAppender.setEncoder(layoutEncoder);

        rollingPolicy.setParent(fileAppender);
        rollingPolicy.start();

        fileAppender.start();

        return fileAppender;
    }

    private FixedWindowRollingPolicy createRollingPolicy(net.microfalx.bootstrap.logger.Appender appender, LoggerContext context) {
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(getFileCount());
        rollingPolicy.setFileNamePattern(".%i.log.gz");
        return rollingPolicy;
    }

    private SizeBasedTriggeringPolicy<ILoggingEvent> createTriggeringPolicy(LoggerContext context) {
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(formatBytes(getFileSize())));
        triggeringPolicy.setCheckIncrement(Duration.buildByMinutes(60));
        triggeringPolicy.start();
        return triggeringPolicy;
    }

    private PatternLayoutEncoder createLayoutEncoder(LoggerContext context) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSSXXX} %-5level [%-15thread] %logger{-36} : %msg%n");
        encoder.start();
        return encoder;
    }

    private int getFileCount() {
        return environment.getProperty("bootstrap.logger.file-count", Integer.class, 5);
    }

    private long getFileSize() {
        return environment.getProperty("bootstrap.logger.file-size", Long.class, 20_000_000L);
    }

    private boolean hasLogsDirectory() {
        File configuredLogsDirectory = getConfiguredLogsDirectory();
        return JvmUtils.hasLogsDirectory() || configuredLogsDirectory != null;
    }

    private File getLogsDirectory() {
        if (logsDirectory == null) {
            File configuredLogsDirectory = getConfiguredLogsDirectory();
            logsDirectory = ObjectUtils.defaultIfNull(configuredLogsDirectory, JvmUtils.getLogsDirectory());
        }
        return logsDirectory;
    }

    private File getConfiguredLogsDirectory() {
        String directory = environment.getProperty("bootstrap.logger.directory");
        return isNotEmpty(directory) && new File(directory).exists() ? new File(directory) : null;
    }

}
