package net.microfalx.bootstrap.support.report;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowException;

/**
 * Identifies a fragment in the report.
 * <p>
 * The fragments are ordered in the report based on {@link Fragment#getOrder()}.
 */
@Getter
@ToString
public class Fragment implements Identifiable<String>, Nameable {

    private final String id;
    private final String name;
    private boolean visible;
    private int order;
    private String icon = "fa-solid fa-notdef";
    private String template;
    private String selector;
    private String description;
    private Resource resource;
    private Throwable throwable;

    ReportService reportService;

    /**
     * Creates a new report fragment builder.
     *
     * @param name the name of the fragment
     * @return a non-null instance
     */
    public static Builder builder(String name) {
        requireNotEmpty(name);
        return builder(name, StringUtils.toIdentifier(name));
    }

    /**
     * Creates a new report fragment builder.
     *
     * @param name the name of the fragment
     * @param id   the identifier of the fragment
     * @return a non-null instance
     */
    public static Builder builder(String name, String id) {
        requireNotEmpty(name);
        requireNotEmpty(id);
        return new Builder(id, name);
    }

    private Fragment(String id, String name) {
        requireNonNull(id);
        requireNonNull(name);
        this.id = id;
        this.name = name;
    }

    /**
     * Renders the template for this fragment.
     *
     * @param resource the resource
     */
    public void render(Report report, Resource resource) {
        requireNonNull(report);
        requireNonNull(resource);
        this.resource = resource;
        try {
            Template template = reportService.createTemplate(this.template).setSelector(selector);
            template.addVariable("report", report);
            template.render(resource);
        } catch (Exception e) {
            throwable = e;
            rethrowException(e);
        }
    }

    /**
     * Returns the icon associated with the fragment.
     *
     * @return a non-null string
     */
    public String getIcon1() {
        return icon;
        /*switch (type) {
            case SUMMARY:
                return "fa-solid fa-list-check";
            case ARTIFACTS:
                return "fa-solid fa-circle-nodes";
            case DEPENDENCIES:
                return "fa-solid fa-hexagon-nodes";
            case ENVIRONMENT:
                return "fa-solid fa-gauge";
            case LOGS:
                return "fa-regular fa-file-lines";
            case FAILURE:
                return "fa-solid fa-triangle-exclamation";
            case PERFORMANCE:
                return "fa-solid fa-flag-checkered";
            case PLUGINS:
                return "fa-solid fa-plug";
            case PROJECT:
                return "fa-solid fa-diagram-project";
            case TESTS:
                return "fa-solid fa-clipboard-check";
            case CODE_COVERAGE:
                return "fa-solid fa-shoe-prints";
            case EXTENSIONS:
                return "fa-solid fa-plug-circle-bolt";
            case TRENDS:
                return "fa-solid fa-arrow-trend-up";
            default:
                return "fa-solid fa-notdef";
        }*/
    }

    /**
     * Returns the content of the fragment.
     *
     * @return a non-null instance
     */
    public String getContent() {
        try {
            return resource.loadAsString();
        } catch (IOException e) {
            return "<div class=\"alert alert-primary\" role=\"alert\">\n" +
                    "Failed to load rendered fragment " + getName() + ", root cause: " + ExceptionUtils.getRootCauseMessage(e) +
                    "    </div>";
        }
    }

    /**
     * A provider which creates a fragment.
     */
    public interface Provider {

        /**
         * Creates a fragment of the report.
         *
         * @return a non-null instance
         */
        Fragment create();

        /**
         * Updates the template before rendering.
         *
         * @param template the template
         */
        default void update(Template template) {
            // empty by default, not all providers need to update the template
        }
    }

    public static class Builder {

        private final String id;
        private final String name;
        private boolean visible = true;
        private int order = 100;
        private String icon = "fa-solid fa-notdef";
        private String template;
        private String selector = "content";
        private String description;

        public Builder(String id, String name) {
            requireNonNull(id);
            requireNonNull(name);
            this.id = StringUtils.toIdentifier(id);
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder visible(boolean enabled) {
            this.visible = enabled;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public Builder selector(String selector) {
            this.selector = selector;
            return this;
        }

        public Fragment build() {
            Fragment fragment = new Fragment(id, name);
            fragment.description = description;
            fragment.visible = visible;
            fragment.order = order;
            fragment.icon = icon;
            fragment.template = template;
            fragment.selector = selector;
            return fragment;
        }
    }
}
