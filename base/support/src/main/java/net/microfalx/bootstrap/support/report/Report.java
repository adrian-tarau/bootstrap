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
    private boolean offline = true;
    private boolean dynamic = true;
    private String fragment;
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
     * Returns whether the report is generated, and it should be viewed offline (outside the application).
     * <p>
     * This does not mean that the report does not need internet access, just that it should be self-contained.
     * <p>
     * By default, reports are offline.
     *
     * @return {@code true} if the report is offline, {@code false} otherwise
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * Changes the offline status of the report.
     *
     * @param offline @code true} to generate an offline report, {@code false} otherwise
     */
    public Report setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    /**
     * Returns whether the report has dynamic content (charts, etc), which requires code execution
     * to be available (JavaScript).
     *
     * @return {@code true} if the report is dynamic, {@code false} otherwise
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Changes whether the report has dynamic content (charts, etc), which requires code execution
     * to be available (JavaScript).
     *
     * @param dynamic {@code true} to generate a dynamic report, {@code false} otherwise
     * @return self
     */
    public Report setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    /**
     * Returns the fragment identifier to be rendered instead of rendering all fragments.
     *
     * @return the fragment identifier, null to render all fragments
     */
    public Optional<String> getFragment() {
        return Optional.ofNullable(fragment);
    }

    /**
     * Sets the fragment identifier to be rendered instead of rendering all fragments.
     *
     * @param fragment the fragment identifier, null to render all fragments
     * @return self
     */
    public Report setFragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    /**
     * Returns whether the report has navigation (multiple fragments).
     *
     * @return {@code true} if the report has navigation, {@code false} otherwise
     */
    public boolean hasNavigation() {
        return this.fragment == null;
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
     * Sets an attribute.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute
     */
    public void setAttribute(String name, Object value) {
        requireNotEmpty(name);
        attributes.put(name, value);
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
                fragment.cleanup();
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
            if (this.fragment != null && !this.fragment.equals(fragment.getId())) continue;
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
