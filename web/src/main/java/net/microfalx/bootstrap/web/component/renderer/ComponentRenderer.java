package net.microfalx.bootstrap.web.component.renderer;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.emptyIfNull;

/**
 * An interface which provides additional rendering rules for components depending on the template engine.
 */
public abstract class ComponentRenderer implements Identifiable<String>, Nameable {

    public static final char CSS_STYLE_SEPARATOR = ';';
    private final Map<Class<?>, String> cssClasses = new HashMap<>();

    /**
     * Returns the CSS classes configured for the component.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public final <C extends Component<C>> String getCssClass(Component<C> component) {
        requireNonNull(component);
        String byTypeClasses = cssClasses.getOrDefault(component.getClass(), StringUtils.EMPTY_STRING);
        byTypeClasses += " " + emptyIfNull(doGetCssClass(component));
        return byTypeClasses.trim();
    }

    /**
     * Returns the CSS styles configured for the component.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public final <C extends Component<C>> String getCssStyle(Component<C> component) {
        requireNonNull(component);
        String styles = emptyIfNull(doGetCssStyle(component));
        return styles.trim();
    }

    /**
     * Registers a class or more
     *
     * @param componentType the component class
     * @param classes       the classes
     * @param <C>           the component type
     */
    protected final <C extends Component<C>> void registerCssClass(Class<C> componentType, String... classes) {
        requireNonNull(componentType);
        cssClasses.put(componentType, String.join(" ", classes));
    }

    /**
     * Returns the CSS classes from component parameters.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public <C extends Component<C>> String doGetCssClass(Component<C> component) {
        return EMPTY_STRING;
    }

    /**
     * Returns the CSS styles from component parameters.
     *
     * @param component the component which provides the classes
     * @return a new list of classes
     */
    public <C extends Component<C>> String doGetCssStyle(Component<C> component) {
        return EMPTY_STRING;
    }

}
