package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.help.HelpService;
import org.thymeleaf.context.IContext;

import static net.microfalx.bootstrap.web.template.TemplateUtils.getModelAttribute;

/**
 * A tool used to render help.
 */
public class HelpTool extends AbstractTool {

    private HelpService helpService;

    public HelpTool(IContext context, HelpService helpService) {
        super(context);
        this.helpService = helpService;
    }

    /**
     * Returns whether the current request has a help reference
     *
     * @return {@code true} if a help reference is available, <code>false</code> otherwise
     */
    public boolean hasHelp() {
        return getModelAttribute(context, "help") != null;
    }

    /**
     * Returns the help reference/path.
     *
     * @return the reference, null if not available
     */
    public String getHelp() {
        return getModelAttribute(context, "help");
    }
}
