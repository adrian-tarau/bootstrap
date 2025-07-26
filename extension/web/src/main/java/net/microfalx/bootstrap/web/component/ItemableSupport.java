package net.microfalx.bootstrap.web.component;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.defaultIfNull;

/**
 * Helper code for all itemable.
 */
public class ItemableSupport implements Itemable<ItemableSupport> {

    private final Component<?> owner;

    private String text;
    private String description;
    private String icon;
    private Style style = Style.BOTH;

    public ItemableSupport(Component<?> owner) {
        requireNonNull(owner);
        this.owner = owner;
    }

    public Component<?> getOwner() {
        return owner;
    }

    public boolean hasText() {
        return text != null;
    }

    public String getText() {
        return text;
    }

    public ItemableSupport setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ItemableSupport setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIcon() {
        return ("icon-font-grow " + defaultIfNull(icon, EMPTY_STRING)).trim();
    }

    public ItemableSupport setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public Style getStyle() {
        return style;
    }

    public ItemableSupport setStyle(Style style) {
        requireNonNull(style);
        this.style = style;
        return this;
    }
}
