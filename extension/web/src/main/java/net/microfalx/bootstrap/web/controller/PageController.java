package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.annotation.Name;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A controller for an application page.
 * <p>
 * By default, all pages require authentication. Use {@link @PermitAll} to allow anonymous access.
 */
public abstract class PageController implements AuthenticatedController {

    public static final String HOME = "index";
    public static final String REDIRECT_HOME = "redirect:/";
    public static final String REDIRECT_LOGIN = "redirect:/login";

    public static final String FRAGMENT_SEPARATOR = "::";
    public static final String MESSAGE_ATTR = "message";

    /**
     * Returns the name of the page.
     * <p>
     * It uses {@link Name} annotation or the simple class name (removes Controller and beautifies the camel-case name).
     *
     * @return the name of the page
     */
    protected String getTitle() {
        Name nameAnnot = AnnotationUtils.getAnnotation(this, Name.class);
        if (nameAnnot != null) {
            return nameAnnot.value();
        } else {
            String simpleName = getClass().getSimpleName();
            simpleName = StringUtils.replaceOnce(simpleName, "Controller", "");
            return net.microfalx.lang.StringUtils.beautifyCamelCase(simpleName);
        }
    }

    /**
     * Update the help reference.
     *
     * @param model the controller model
     * @see Help
     */
    protected final void updateHelp(Model model) {
        requireNonNull(model);
        Help helpAnnot = AnnotationUtils.getAnnotation(this, Help.class);
        if (helpAnnot != null) {
            model.addAttribute("help", helpAnnot.value());
        }
    }

    /**
     * Update the title of the page.
     *
     * @param model the controller model
     * @see #getTitle()
     */
    protected final void updateTitle(Model model) {
        requireNonNull(model);
        model.addAttribute("title", getTitle());
    }

    /**
     * Updates the model with a message which will be displayed to the user.
     *
     * @param model   the controller model
     * @param message the message to display
     */
    protected final void updateUserMessage(Model model, String message) {
        model.addAttribute(MESSAGE_ATTR, message);
    }

    /**
     * Returns the message which will be displayed to the user.
     *
     * @param model the model
     * @return the message, null if there is no message
     */
    protected final String getUserMessage(Model model) {
        return (String) model.getAttribute(MESSAGE_ATTR);
    }
}
