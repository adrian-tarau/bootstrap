package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.support.SupportProperties;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.split;

@Service
@Slf4j
public class ReportService implements InitializingBean {

    @Autowired(required = false) private SupportProperties properties = new SupportProperties();
    @Autowired private ThreadPool threadPool;
    @Autowired private ApplicationContext applicationContext;

    private final Collection<Fragment.Provider> providers = new CopyOnWriteArrayList<>();
    private final Collection<ReportingListener> listeners = new CopyOnWriteArrayList<>();
    private volatile TemplateEngine templateEngine;
    private volatile long lastRenderingTime = TimeUtils.oneHourAgo();

    /**
     * Returns registered providers.
     *
     * @return a non-null instance
     */
    public Collection<Fragment.Provider> getProviders() {
        return Collections.unmodifiableCollection(providers);
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
        report.setEndTime(ZonedDateTime.now());
        report.setStartTime(report.getEndTime().minus(interval));
        report.setName("System Report - " + getSystemName());
        Resource fullReport = Resource.temporary("report_full", ".html");
        try {
            report.render(fullReport);
        } catch (Exception e) {
            throw new ReportException("Failed to render the report for interval " + interval, e);
        }
        try {
            send(report, fullReport, fullReport);
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
        if (properties.isReportOnBoot()) {
            threadPool.execute(new SendReportTask(Duration.ofHours(1)));
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
        LOGGER.info("Create report, providers loaded: {}", providers.size());
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
        loadedListeners.addAll(applicationContext.getBeansOfType(ReportingListener.class).values());
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
        Set<String> destinations = new HashSet<>(Arrays.asList(split(properties.getReportRecipients(), ",")));
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
    }

    private void initTasks() {
        threadPool.schedule(new SendReportTask(Duration.ofHours(24)), new CronTrigger(properties.getReportSchedule()));
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
        template.addVariable("application", new Application());
        template.addVariable("helper", new ReportHelper());
        for (ReportingListener listener : listeners) {
            listener.update(template);
        }
        for (Fragment.Provider provider : providers) {
            provider.update(template);
        }
    }

    private String getSystemName() {
        String systemName = properties.getReportSystemName();
        if (isEmpty(systemName)) {
            try {
                systemName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                systemName = "Local";
            }
        }
        return systemName;
    }

    private class SendReportTask implements Runnable {

        private final Duration interval;

        public SendReportTask(Duration interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            send(interval);
        }
    }

    private class ReleaseEngineTask implements Runnable {

        @Override
        public void run() {
            if (templateEngine == null) return;
            if (TimeUtils.millisSince(lastRenderingTime) > TimeUtils.ONE_MINUTE) {
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


}
