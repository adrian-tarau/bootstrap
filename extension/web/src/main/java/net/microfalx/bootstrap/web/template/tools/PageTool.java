package net.microfalx.bootstrap.web.template.tools;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import static net.microfalx.bootstrap.web.template.TemplateUtils.*;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Template utilities around pages.
 */
public class PageTool extends AbstractTool {

    private static final String PAGE_TITLE_WAS_CONSUMED = "page_title_consumed";

    public PageTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
    }

    /**
     * Returns whether the page has a title.
     * <p>
     * Once the {@link #getTitle()} is called, this method returns false. This allows templates to use the title once,
     * in one place.
     *
     * @return {@code true} if it has a title, {@code false otherwise}
     */
    public boolean hasTitle() {
        return isNotEmpty(doGetTitle()) && !containsModelAttribute(getTemplateContext(), PAGE_TITLE_WAS_CONSUMED);
    }

    /**
     * Returns the page title.
     *
     * @return the page title, null if there is no title
     * @see #hasTitle()
     */
    public String getTitle() {
        try {
            return doGetTitle();
        } finally {
            setModelAttribute(getTemplateContext(), PAGE_TITLE_WAS_CONSUMED, Boolean.TRUE);
        }
    }

    private String doGetTitle() {
        return getModelAttribute(getTemplateContext(), "title");
    }
}
