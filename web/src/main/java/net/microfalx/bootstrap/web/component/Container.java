package net.microfalx.bootstrap.web.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ObjectUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for all containers.
 *
 * @param <C> the container type
 */
public class Container<C extends Container<C>> extends Component<C> {

    private volatile List<Component<? extends Component<?>>> children = new ArrayList<>(0);

    /**
     * Returns a collection with all children.
     *
     * @return a non-null instance
     */
    public final Collection<Component<? extends Component<?>>> getChildren() {
        return unmodifiableCollection(children);
    }

    /**
     * Returns a child component at a given index.
     *
     * @param index the index of the child
     * @return the child
     */
    public Component<? extends Component<?>> getChild(int index) {
        if (children == null) throw new ArrayIndexOutOfBoundsException(index);
        return children.get(index);
    }

    /**
     * Returns whether the container has children.
     *
     * @return {@code true} if it has children, {@code false} otherwise
     */
    public final boolean hasChildren() {
        return isNotEmpty(children);
    }

    /**
     * Adds a component as a child of this container.
     *
     * @param component the component
     * @param <CC>      the component type
     */
    public final <CC extends Component<CC>> C add(CC component) {
        requireNonNull(component);
        if (find(component.getId()) != null) return self();
        children.add(component);
        component.setParent(this);
        if (component.getPosition() < 0) component.setPosition(children.size());
        children.sort(Comparator.comparingInt(Component::getPosition));
        return self();
    }

    /**
     * Removes a component from this container.
     *
     * @param component the component
     * @param <CC>      the component type
     */
    public final <CC extends Component<CC>> C remove(CC component) {
        requireNonNull(component);
        children.remove(component);
        return self();
    }

    /**
     * Finds a direct child with a given identifier.
     *
     * @param id the child identifier
     * @return the child, null if no such child exist
     */
    public final <CC extends Component<CC>> CC find(String id) {
        return find(id, false);
    }

    /**
     * Finds a child with a given identifier.
     *
     * @param id        the child identifier
     * @param recursive {@code true} to find the child at any level, {@code false} at first level
     * @return the child, null if no such child exist
     */
    @SuppressWarnings("unchecked")
    public final <CC extends Component<CC>> CC find(String id, boolean recursive) {
        requireNotEmpty(id);
        id = toIdentifier(id);
        for (Component<? extends Component<?>> child : children) {
            if (id.equals(child.getId())) return (CC) child;
            if (recursive && child instanceof Container) {
                child = ((Container<?>) child).find(id, true);
                if (child != null) return (CC) child;
            }
        }
        return null;
    }
}
