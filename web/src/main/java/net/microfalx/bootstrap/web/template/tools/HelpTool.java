package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.help.HelpService;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import static net.microfalx.bootstrap.web.template.TemplateUtils.getModelAttribute;

/**
 * A tool used to render help.
 */
public class HelpTool extends AbstractTool {

    private HelpService helpService;

    public HelpTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
        this.helpService = applicationContext.getBean(HelpService.class);
    }

    /**
     * Returns whether the current request has a help reference
     *
     * @return {@code true} if a help reference is available, <code>false</code> otherwise
     */
    public boolean hasHelp() {
        return getModelAttribute(templateContext, "help") != null;
    }

    /**
     * Returns the help reference/path.
     *
     * @return the reference, null if not available
     */
    public String getHelp() {
        return getModelAttribute(templateContext, "help");
    }
}
