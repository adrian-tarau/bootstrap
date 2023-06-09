package net.microfalx.bootstrap.web.component;

import java.util.Set;

/**
 * Base class for all components with an action.
 *
 * @param <C> the component type
 */
public class ActionableComponent<C extends ActionableComponent<C>> extends Component<C> implements Actionable<C> {

    private final ActionableSupport actionable = new ActionableSupport(this);

    @Override
    public final String getAction() {
        return actionable.getAction();
    }

    @Override
    public final C setAction(String action) {
        actionable.setAction(action);
        return self();
    }

    @Override
    public final String getToken() {
        return actionable.getToken();
    }

    @Override
    public final C setToken(String token) {
        actionable.setToken(token);
        return self();
    }

    @Override
    public final String getHandler() {
        return actionable.getHandler();
    }

    @Override
    public final C setHandler(String handler) {
        actionable.setToken(handler);
        return self();
    }

    @Override
    public final Set<String> getRoles() {
        return actionable.getRoles();
    }

    @Override
    public final C setRoles(String... roles) {
        actionable.setRoles(roles);
        return self();
    }

    @Override
    public final C addRoles(String... roles) {
        actionable.addRoles(roles);
        return self();
    }

    @Override
    public final boolean hasText() {
        return actionable.hasText();
    }

    @Override
    public final String getText() {
        return actionable.getText();
    }

    @Override
    public final C setText(String text) {
        actionable.setText(text);
        return self();
    }

    @Override
    public final String getIcon() {
        return actionable.getIcon();
    }

    @Override
    public final C setIcon(String icon) {
        actionable.setIcon(icon);
        return self();
    }

    @Override
    public final Style getStyle() {
        return actionable.getStyle();
    }

    @Override
    public final C setStyle(Style style) {
        actionable.setStyle(style);
        return self();
    }
}
