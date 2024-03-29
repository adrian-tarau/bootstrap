package net.microfalx.bootstrap.web.component.panel;

import net.microfalx.bootstrap.web.component.Container;
import net.microfalx.bootstrap.web.component.Toolbar;

/**
 * Base container for all panels.
 *
 * @param <C> the container type
 */
public abstract class BasePanel<C extends BasePanel<C>> extends Container<C> {

    private String title;
    private boolean header;
    private boolean collapsible;
    private boolean collapsed;
    private final Toolbar top = new Toolbar();
    private final Toolbar bottom = new Toolbar();

    public String getTitle() {
        return title;
    }

    public C setTitle(String title) {
        this.title = title;
        return self();
    }

    public boolean isHeader() {
        return header;
    }

    public C setHeader(boolean header) {
        this.header = header;
        return self();
    }

    public boolean isCollapsible() {
        return collapsible;
    }

    public C setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
        return self();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public C setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        return self();
    }

    public Toolbar getTop() {
        return top;
    }

    public Toolbar getBottom() {
        return bottom;
    }
}
