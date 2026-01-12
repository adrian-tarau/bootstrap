package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.support.SupportProperties;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.threadpool.ThreadPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.StandardCacheManager;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
@Slf4j
public class ReportService implements InitializingBean {

    @Autowired(required = false) private SupportProperties properties = new SupportProperties();
    @Autowired private ThreadPool threadPool;
    @Autowired private ApplicationContext applicationContext;

    private final Collection<Fragment.Provider> providers = new CopyOnWriteArrayList<>();
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

    @Override
    public void afterPropertiesSet() throws Exception {
        loadProviders();
        initVariables();
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
        LOGGER.info("Loaded {} report providers", loadedProviders.size());
    }

    private void initVariables() {
        LOGGER.info("Startup time: {} ", new ReportHelper().getStartupTime());
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
        for (Fragment.Provider provider : providers) {
            provider.update(template);
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
