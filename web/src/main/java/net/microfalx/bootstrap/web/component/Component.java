package net.microfalx.bootstrap.web.component;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all components.
 *
 * @param <C> the component type
 */
public abstract class Component<C extends Component<C>> implements Identifiable<String> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private String id = generateIdentifier();
    private Container<?> parent;
    private String width;
    private String height;
    private String tooltip;
    private boolean visible;
    private boolean disabled;
    private boolean readOnly;
    int position = -1;

    @Override
    public final String getId() {
        return id;
    }

    /**
     * Changes the component identifier.
     * <p>
     * The identifiers are normalized, see {@link StringUtils#toIdentifier(String)}.
     *
     * @param id the identifier
     * @return self
     */
    public final C setId(String id) {
        this.id = StringUtils.toIdentifier(id);
        return self();
    }

    /**
     * Returns the type for the component.
     * <p>
     * The type is a short alias for component which can be used to find components.
     * <p>
     * By default, the type is equal with the simple class name.
     *
     * @return a non-null instance.
     */
    public String getType() {
        return StringUtils.toIdentifier(getClass().getSimpleName());
    }

    /**
     * Returns the immediate parent of this component.
     *
     * @return the parent, null if this component is top level component
     */
    public final Container<?> getParent() {
        return parent;
    }

    /**
     * Changes the parent of this component.
     *
     * @param parent the parent, null to unparent a component
     */
    protected final void setParent(Container<?> parent) {
        if (parent == null && this.parent != null) this.parent.remove(self());
        this.parent = parent;
    }

    /**
     * Returns the first parent with a given type.
     *
     * @param type the class of the parent component
     * @param <P>  the type of the parent
     * @return the parent, null if such a parent does not exist
     */
    @SuppressWarnings("unchecked")
    public final <P extends Component<P>> P getParent(Class<P> type) {
        Container<?> parent = this.parent;
        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) return (P) parent;
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Returns the width of the component.
     *
     * @return the width (pixels, percentage, units), null if not set
     */
    public String getWidth() {
        return width;
    }

    /**
     * Changes the width of the component
     *
     * @param width the width (pixels, percentage, units), null if not set
     */
    public C setWidth(String width) {
        this.width = width;
        return self();
    }

    /**
     * Returns the height of the component.
     *
     * @return the width (pixels, percentage, units), null if not set
     */
    public String getHeight() {
        return height;
    }

    /**
     * Changes the height of the component
     *
     * @param height the width (pixels, percentage, units), null if not set
     */
    public C setHeight(String height) {
        this.height = height;
        return self();
    }

    /**
     * Returns whether the component is visible.
     *
     * @return <code>true</code> if visible,<code>false</code> otherwise
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Changes the visibility attribute of the component.
     *
     * @param visible <code>true</code> if visible,<code>false</code> otherwise
     * @return self
     */
    public final C setVisible(boolean visible) {
        this.visible = visible;
        return self();
    }

    /**
     * Returns the tooltip associated with the component.
     *
     * @return the tooltip, null if not set
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Changes the tooltip.
     *
     * @param tooltip the tooltip
     */
    public C setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return self();
    }

    /**
     * Returns whether the component is read-only (content/items) cannot be changed.
     *
     * @return @{code true} if read-only,<code>false</code> otherwise
     */
    public final boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Changes the read-only attribute of the component.
     *
     * @param readOnly @{code true} if read-only,<code>false</code> otherwise
     * @return self
     */
    public final C setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return self();
    }

    /**
     * Returns whether the component is disabled (users cannot interact with it, grayed out).
     *
     * @return @{code true} if disabled,<code>false</code> otherwise
     */
    public final boolean isDisabled() {
        return disabled;
    }

    /**
     * Changes the disabled attribute of the component.
     *
     * @param disabled @{code true} if disabled,<code>false</code> otherwise
     * @return self
     */
    public final C setDisabled(boolean disabled) {
        this.disabled = disabled;
        return self();
    }

    /**
     * Returns the position of the component.
     * <p>
     * The position is used to render the children in a desired order. If no position is set, the component will be added at the end of the list.
     *
     * @return the position
     */
    public final int getPosition() {
        return position;
    }

    /**
     * Changes the position of this component in its parent.
     *
     * @param position the position
     * @return self
     */
    public final C setPosition(int position) {
        this.position = position;
        return self();
    }

    @Override
    public String toString() {
        return "Component{" +
                "id='" + id + '\'' +
                ", parent=" + (parent != null ? parent.getId() : null) +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", visible=" + visible +
                ", disabled=" + disabled +
                ", readOnly=" + readOnly +
                ", position=" + position +
                '}';
    }

    @SuppressWarnings("unchecked")
    protected final C self() {
        return (C) this;
    }

    private String generateIdentifier() {
        return StringUtils.toIdentifier(getClass().getSimpleName()) + ID_GENERATOR.getAndIncrement();
    }
}
