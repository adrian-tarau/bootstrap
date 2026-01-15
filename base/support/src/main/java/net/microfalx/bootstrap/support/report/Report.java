package net.microfalx.bootstrap.support.report;

import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowException;

/**
 * Holds the system report.
 */
public class Report implements Nameable {

    private final ReportService reportService;

    private String name = "System Report";
    private ZonedDateTime startTime = ZonedDateTime.now().minusHours(24);
    private ZonedDateTime endTime = ZonedDateTime.now();
    private boolean failOnError;
    private final List<Fragment> fragments = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();

    private static final ThreadLocal<Report> CURRENT_REPORT = new ThreadLocal<>();

    public static Report current() {
        Report report = CURRENT_REPORT.get();
        if (report == null) {
            throw new IllegalStateException("A report instance is not available in thread "
                    + Thread.currentThread().getName());
        }
        return report;
    }

    Report(ReportService reportService) {
        requireNonNull(reportService);
        this.reportService = reportService;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Changes the name of the report
     *
     * @param name the new name
     * @return self
     */
    public Report setName(String name) {
        requireNonNull(name);
        this.name = name;
        return this;
    }

    /**
     * Returns a collection of exceptions encountered during report generation.
     *
     * @return a non-null instance
     */
    public Collection<Fragment> getFragments() {
        return unmodifiableCollection(fragments);
    }

    /**
     * Changes whether the report build should fail due to an error in one of the fragments.
     *
     * @param failOnError <code>true</code> to fail the report on
     */
    public Report setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    /**
     * Returns the start time.
     *
     * @return a non-null instance
     */
    public ZonedDateTime getStartTime() {
        return startTime;
    }

    /**
     * Changes the start time.
     *
     * @param startTime a new start time
     * @return self
     */
    public Report setStartTime(ZonedDateTime startTime) {
        requireNotEmpty(startTime);
        this.startTime = startTime;
        return this;
    }

    /**
     * Returns the report end time.
     *
     * @return a non-null instance
     */
    public ZonedDateTime getEndTime() {
        return endTime;
    }

    /**
     * Changes the report end time.
     *
     * @param endTime the end time
     * @return self
     */
    public Report setEndTime(ZonedDateTime endTime) {
        requireNotEmpty(startTime);
        this.endTime = endTime;
        return this;
    }

    /**
     * Returns the report time interval.
     *
     * @return a non-null instance
     */
    public Duration getInterval() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Returns a cached attribute.
     *
     * @param name                 the name of the attribute
     * @param defaultValueSupplier the supplier for the default value
     * @param <A>                  the type of the attribute
     * @return the attribute value
     */
    @SuppressWarnings("unchecked")
    public <A> A getAttribute(String name, Supplier<A> defaultValueSupplier) {
        requireNotEmpty(name);
        requireNonNull(defaultValueSupplier);
        return (A) attributes.computeIfAbsent(name, s -> defaultValueSupplier.get());
    }

    /**
     * Renders the report.
     *
     * @param resource the resource
     * @throws IOException if an I/O error occurs
     */
    public void render(Resource resource) throws IOException {
        requireNonNull(resource);
        CURRENT_REPORT.set(this);
        try {
            buildFragments();
            Template template = reportService.createTemplate("report");
            updateTemplate(template);
            template.render(resource);
        } finally {
            CURRENT_REPORT.remove();
            cleanup();
        }
    }

    /**
     * Cleanups temporary resources associated with the report
     */
    public void cleanup() {
        for (Fragment fragment : fragments) {
            if (fragment.getResource() != null) {
                try {
                    fragment.getResource().delete();
                } catch (Exception e) {
                    // not important
                }
            }
        }
    }

    void registerFragment(Fragment fragment) {
        requireNonNull(fragment);
        fragments.add(fragment);
        fragments.sort(Comparator.comparing(Fragment::getOrder));
    }

    private void buildFragments() {
        for (Fragment fragment : fragments) {
            if (!fragment.isVisible()) continue;
            Resource temporary = Resource.temporary("support_report_" + fragment.getId() + "_", ".html");
            try {
                fragment.render(this, temporary);
            } catch (Exception e) {
                if (failOnError) rethrowException(e);
            }
        }
    }

    private void updateTemplate(Template template) {
        template.addVariable("report", this);
        template.addVariable("fragments", getVisibleFragments());
        updateCodeFragments(template);
    }

    private void updateCodeFragments(Template template) {
        List<String> fragmentTemplates = getVisibleFragments().stream().map(Fragment::getTemplate).toList();
        template.addVariable("fragmentTemplates", fragmentTemplates);
    }

    private Collection<Fragment> getVisibleFragments() {
        return fragments.stream().filter(Fragment::isVisible).toList();
    }

}
