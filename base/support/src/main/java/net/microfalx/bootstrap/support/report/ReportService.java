package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.logger.LoggerEvent;
import net.microfalx.bootstrap.logger.LoggerListener;
import net.microfalx.lang.*;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.CronTrigger;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static java.time.Duration.ofHours;
import static java.util.Collections.unmodifiableCollection;
import static net.lingala.zip4j.model.enums.CompressionLevel.MAXIMUM;
import static net.lingala.zip4j.model.enums.CompressionMethod.DEFLATE;
import static net.lingala.zip4j.model.enums.EncryptionMethod.ZIP_STANDARD;
import static net.microfalx.bootstrap.support.report.Template.APPLICATION_VARIABLE;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.EnumUtils.toLabel;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.IOUtils.getBufferedOutputStream;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.split;
import static net.microfalx.lang.TimeUtils.*;

@Service
@Slf4j
public class ReportService implements InitializingBean {

    @Autowired(required = false) private ReportProperties properties = new ReportProperties();
    @Autowired private ThreadPool threadPool;
    @Autowired private ApplicationContext applicationContext;

    private final Collection<Fragment.Provider> providers = new CopyOnWriteArrayList<>();
    private final Collection<ReportingListener> listeners = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();
    private volatile TemplateEngine templateEngine;
    private volatile long lastRenderingTime = TimeUtils.oneHourAgo();
    private volatile long lastIssuesUpdate = TimeUtils.oneHourAgo();
    private volatile Collection<Issue> cachedIssues = Collections.emptyList();
    private volatile Collection<Issue> cachedDailyIssues;
    private volatile Map<String, Issue> reportedIssues = new ConcurrentHashMap<>();
    private volatile Map<String, Issue> dailyReportedIssues = new ConcurrentHashMap<>();

    private static final ThreadLocal<Boolean> DAILY_REPORT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Returns registered providers.
     *
     * @return a non-null instance
     */
    public Collection<Fragment.Provider> getProviders() {
        return unmodifiableCollection(providers);
    }

    /**
     * Returns registered issues.
     *
     * @return a non-null instance
     */
    public Collection<Issue> getIssues() {
        updateIssuesCache();
        if (isDailyReport()) {
            synchronized (lock) {
                if (cachedDailyIssues == null) {
                    Map<String, Issue> allIssues = new HashMap<>(dailyReportedIssues);
                    cachedIssues.forEach(issue -> mergeIssue(allIssues, issue));
                    cachedDailyIssues = new ArrayList<>(allIssues.values());
                }
            }
            return unmodifiableCollection(cachedDailyIssues);
        } else {
            return unmodifiableCollection(cachedIssues);
        }
    }

    /**
     * Returns the number of issues with the given severity or higher.
     *
     * @param severity the severity
     * @return the number of issues
     */
    public int getIssueCount(Issue.Severity severity) {
        return (int) getIssues().stream().filter(issue -> issue.getSeverity().ordinal() >= severity.ordinal()).mapToLong(Issue::getOccurrences).sum();
    }

    /**
     * Reports an issue.
     * <p>
     * Services can call this method to report issues they detect directly. However, services can also report
     * issues using {@link ReportingListener#getIssues()}.
     *
     * @param issue the issue
     */
    public void addIssue(Issue issue) {
        requireNonNull(issue);
        synchronized (lock) {
            mergeIssue(reportedIssues, issue);
        }
    }

    /**
     * Returns a provider by its class.
     *
     * @param providerClass the provider class
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <P extends Fragment.Provider> P getProviderByClass(Class<P> providerClass) {
        requireNonNull(providerClass);
        for (Fragment.Provider provider : providers) {
            if (provider.getClass().equals(providerClass)) {
                return (P) provider;
            }
        }
        throw new IllegalArgumentException("No provider found for class " + providerClass.getName());
    }

    /**
     * Sends a report about the system, using the given duration as the report time interval.
     *
     * @param interval the reporting interval
     */
    public void send(Duration interval) {
        requireNonNull(interval);
        Report report = createReport();
        updateReportName(report);
        report.setEndTime(ZonedDateTime.now());
        report.setStartTime(report.getEndTime().minus(interval));
        Resource fullReport = Resource.temporary("report_", ".html");
        try {
            report.render(fullReport);
            fullReport = encryptReport(fullReport);
        } catch (Exception e) {
            throw new ReportException("Failed to render the report for interval " + interval, e);
        }
        report.setDynamic(false).setFragment("summary");
        Resource summaryReport = Resource.temporary("report_summary_", ".html");
        try {
            report.render(summaryReport);
        } catch (Exception e) {
            throw new ReportException("Failed to render the report for interval " + interval, e);
        }
        try {
            send(report, summaryReport, fullReport);
        } catch (Exception e) {
            throw new ReportException("Failed to send the report for interval " + interval, e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadProviders();
        loadListeners();
        initVariables();
        initTasks();
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (properties.isEnabled() && properties.isOnBoot()) {
            threadPool.execute(new SendReportTask(ofHours(1)));
        }
    }

    /**
     * Creates a template used by the report service.
     *
     * @param name the name of the template
     * @return a non-null instance
     */
    public Template createTemplate(String name) {
        requireNonNull(name);
        initEngine();
        Template template = new Template(templateEngine, name);
        updateTemplate(template);
        return template;
    }

    /**
     * Creates a report by aggregating all fragments from the providers.
     *
     * @return a non-null instance
     */
    public Report createReport() {
        initEngine();
        LOGGER.debug("Create report, providers loaded: {}", providers.size());
        Report report = new Report(this);
        for (Fragment.Provider provider : providers) {
            Fragment fragment = provider.create();
            fragment.reportService = this;
            report.registerFragment(fragment);
        }
        return report;
    }

    private void loadProviders() {
        LOGGER.debug("Loading report providers");
        Collection<Fragment.Provider> loadedProviders = ClassUtils.resolveProviderInstances(Fragment.Provider.class);
        for (Fragment.Provider loadedProvider : loadedProviders) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedProvider));
            if (loadedProvider instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(applicationContext);
            }
            providers.add(loadedProvider);
        }
        LOGGER.info("Loaded {} report providers", providers.size());
    }

    private void loadListeners() {
        LOGGER.debug("Loading reporting listeners");
        Collection<ReportingListener> loadedListeners = ClassUtils.resolveProviderInstances(ReportingListener.class);
        for (ReportingListener loadedProvider : loadedListeners) {
            LOGGER.debug(" - {}", ClassUtils.getName(loadedProvider));
            if (loadedProvider instanceof ApplicationContextAware applicationContextAware) {
                applicationContextAware.setApplicationContext(applicationContext);
            }
            listeners.add(loadedProvider);
        }
        LOGGER.info("Loaded {} reporting listeners", listeners.size());
    }

    private void send(Report report, Resource summary, Resource fullReport) {
        Set<String> destinations = new HashSet<>(Arrays.asList(split(properties.getRecipients(), ",")));
        if (destinations.isEmpty()) {
            LOGGER.info("No report destinations configured, ask services");
            for (ReportingListener listener : listeners) {
                destinations.addAll(listener.getDestinations());
            }
        }
        for (ReportingListener listener : listeners) {
            listener.send(destinations, report.getName(), summary, Optional.of(fullReport));
        }
        LOGGER.info("Report sent to: {}", destinations);
    }

    private void initVariables() {
        LOGGER.info("Startup time: {} ", new ReportHelper().getStartupTime());
        if (properties.isEnabled()) {
            LOGGER.info("Support report scheduled at '{}/{}/{}', recipients: '{}'", properties.getDailySchedule(), properties.getWithIssuesSchedule(), properties.getWithCriticalIssuesSchedule(), properties.getRecipients());
        }
    }

    private void initTasks() {
        threadPool.schedule(new SendReportTask(ofHours(24)), new CronTrigger(properties.getDailySchedule()));
        threadPool.schedule(new IssuesReportTask(Issue.Severity.MEDIUM), new CronTrigger(properties.getWithIssuesSchedule()));
        threadPool.schedule(new IssuesReportTask(Issue.Severity.CRITICAL), new CronTrigger(properties.getWithCriticalIssuesSchedule()));
    }

    private synchronized void initEngine() {
        if (templateEngine != null) return;
        LOGGER.info("Initializing template engine");
        // init resolver
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver(Template.class.getClassLoader());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/support/report/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setCacheable(false);
        // create engine
        templateEngine = new TemplateEngine();
        templateEngine.setDialect(new StandardDialect());
        templateEngine.setLinkBuilder(new StandardLinkBuilder());
        templateEngine.setCacheManager(new StandardCacheManager());
        templateEngine.setTemplateResolver(templateResolver);
        lastRenderingTime = currentTimeMillis();
        threadPool.schedule(new ReleaseEngineTask(), 5, TimeUnit.MINUTES);
    }

    private void updateTemplate(Template template) {
        template.addVariable("helper", new ReportHelper());
        template.addVariable("issues", getIssues());
        for (ReportingListener listener : listeners) {
            listener.update(template);
        }
        for (Fragment.Provider provider : providers) {
            provider.update(template);
        }
        if (!template.hasVariable(APPLICATION_VARIABLE)) {
            template.addVariable(APPLICATION_VARIABLE, new Application());
        }
    }

    private void updateReportName(Report report) {
        String name = "System Report - " + getSystemName();
        int issueCriticalCount = getIssueCount(Issue.Severity.CRITICAL);
        int issueHighCount = getIssueCount(Issue.Severity.HIGH);
        int issueMediumHighCount = getIssueCount(Issue.Severity.MEDIUM);
        int issueLowCount = getIssueCount(Issue.Severity.LOW);
        name += " (Critical: " + issueCriticalCount + ", High: " + issueHighCount + ", Medium: " + issueMediumHighCount
                + ", Low: " + issueLowCount + ")";
        report.setName(name);
    }

    private String getSystemName() {
        String systemName = properties.getSystemName();
        if (isEmpty(systemName)) {
            try {
                systemName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                systemName = "Local";
            }
        }
        return systemName;
    }

    private Resource encryptReport(Resource report) throws IOException {
        if (StringUtils.isEmpty(properties.getPassword())) return report;
        String systemName = StringUtils.toIdentifier(properties.getSystemName());
        String reportPrefix = "support_report_" + systemName;
        File reportZip = new File(JvmUtils.getTemporaryDirectory(), getFileName(reportPrefix, "zip"));
        try (ZipOutputStream outputZipStream = new ZipOutputStream(getBufferedOutputStream(reportZip),
                properties.getPassword().toCharArray())) {
            //init the zip parameters
            ZipParameters zipParams = new ZipParameters();
            zipParams.setCompressionMethod(DEFLATE);
            zipParams.setCompressionLevel(MAXIMUM);
            zipParams.setEncryptFiles(true);
            zipParams.setEncryptionMethod(ZIP_STANDARD);
            zipParams.setFileNameInZip(getFileName(reportPrefix, "html"));
            //create zip entry
            outputZipStream.putNextEntry(zipParams);
            IOUtils.appendStream(outputZipStream, report.getInputStream(), false);
            outputZipStream.closeEntry();
        }
        return Resource.file(reportZip);
    }

    private void extractAndSendIssues(Issue.Severity severity) {
        REPORT.count("Check Issues: " + severity.name());
        updateIssuesCache();
        if (cachedIssues.isEmpty()) {
            LOGGER.info("No issues found with severity {}", severity);
        } else {
            long criticalIssuesCount = getIssueCount(Issue.Severity.CRITICAL);
            long highIssuesCount = getIssueCount(Issue.Severity.HIGH);
            // if it was requested to send only critical issues, but there are none, skip sending
            if (criticalIssuesCount > 0 && severity != Issue.Severity.CRITICAL) return;
            // if there are issues, sent the report, looking at the last hour
            boolean shouldSend = criticalIssuesCount >= properties.getCriticalIssuesThreshold() ||
                    highIssuesCount >= properties.getHighIssuesThreshold();
            if (shouldSend) {
                REPORT.count("Sent: " + severity.name());
                send(Duration.ofHours(1));
            }
        }

    }

    private synchronized void updateIssuesCache() {
        if (millisSince(lastIssuesUpdate) < FIVE_MINUTE) return;
        REPORT.count("Update Issues");
        Map<String, Issue> issues = new HashMap<>();
        for (ReportingListener listener : listeners) {
            Collection<Issue> listenerIssues = listener.getIssues();
            if (listenerIssues != null) listenerIssues.forEach(issue -> issues.put(issue.getId(), issue));
        }
        pollIssues();
        // take all reported issues, accumulate them for the 24h report, and start fresh for the next interval
        synchronized (lock) {
            issues.putAll(reportedIssues);
            dailyReportedIssues.putAll(reportedIssues);
            reportedIssues = new ConcurrentHashMap<>();
        }
        cachedIssues = new ArrayList<>(issues.values());
        lastIssuesUpdate = currentTimeMillis();
    }

    private String getFileName(String prefix, String extension) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        return prefix + "_" + timestamp + "." + extension;
    }

    private void pollIssues() {
        while (!Issue.ISSUES.isEmpty()) {
            addIssue(Issue.ISSUES.poll());
        }
    }

    private static boolean isDailyReport() {
        return DAILY_REPORT.get();
    }

    private void mergeIssue(Map<String, Issue> issues, Issue issue) {
        Issue previous = issues.putIfAbsent(issue.getId(), issue);
        if (previous != null) {
            issue = previous.withDetectedAt(issue.getLastDetectedAt());
            issues.put(issue.getId(), issue);
        }
    }

    @Provider
    public static class ReportingLoggerListener extends ApplicationContextSupport implements LoggerListener {

        @Override
        public void onEvent(LoggerEvent event) {
            if (!event.getLevel().isHigherSeverity(LoggerEvent.Level.WARN)) return;
            Issue.create(Issue.Type.STABILITY, toLabel(event.getLevel())).withModule("Logger")
                    .withSeverity(event.getLevel() == LoggerEvent.Level.WARN ? Issue.Severity.LOW : Issue.Severity.MEDIUM)
                    .withDescription("Most recent entry: " + event.getMessage())
                    .register();
        }
    }

    private class IssuesReportTask implements Runnable {

        private final Issue.Severity severity;

        public IssuesReportTask(Issue.Severity severity) {
            this.severity = severity;
        }

        @Override
        public void run() {
            extractAndSendIssues(severity);
        }
    }

    private class SendReportTask implements Runnable {

        private final Duration interval;

        public SendReportTask(Duration interval) {
            this.interval = interval;
        }

        private void cleanup() {
            if (isDailyReport()) {
                synchronized (lock) {
                    dailyReportedIssues = new ConcurrentHashMap<>();
                    cachedDailyIssues = null;
                }
            }
            DAILY_REPORT.remove();
        }

        @Override
        public void run() {
            DAILY_REPORT.set(interval.toHours() >= 24);
            try {
                REPORT.count("Send Report: " + formatDuration(interval));
                send(interval);
            } finally {
                cleanup();
            }
        }
    }

    private class ReleaseEngineTask implements Runnable {

        @Override
        public void run() {
            if (templateEngine == null) return;
            if (millisSince(lastRenderingTime) > ONE_MINUTE) {
                templateEngine = null;
                LOGGER.info("Template engine released due to inactivity");
            }
        }
    }

    @Getter
    @ToString
    private static class Application {
        private final String url = "http://localhost:8080";
        private final String name = "Test";
        private final String owner = "The team";
    }

    private static final Metrics REPORT = Metrics.of("Support").withGroup("Report");


}
