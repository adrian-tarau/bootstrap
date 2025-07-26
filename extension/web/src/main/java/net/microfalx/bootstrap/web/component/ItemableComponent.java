package net.microfalx.bootstrap.web.component;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Base class for all components represented as an item.
 *
 * @param <C> the component type
 */
public abstract class ItemableComponent<C extends ItemableComponent<C>> extends Component<C> implements Itemable<C> {

    private final ItemableSupport itemable = new ItemableSupport(this);

    @Override
    public final boolean hasText() {
        return itemable.hasText();
    }

    @Override
    public final String getText() {
        return itemable.getText();
    }

    @Override
    public final C setText(String text) {
        itemable.setText(text);
        return self();
    }

    @Override
    public final String getIcon() {
        return itemable.getIcon();
    }

    @Override
    public final C setIcon(String icon) {
        itemable.setIcon(icon);
        return self();
    }

    @Override
    public final Style getStyle() {
        return itemable.getStyle();
    }

    @Override
    public final C setStyle(Style style) {
        itemable.setStyle(style);
        return self();
    }

    @Override
    public String getDescription() {
        return defaultIfEmpty(itemable.getDescription(), super.getDescription());
    }

    @Override
    public C setDescription(String description) {
        super.setDescription(description);
        itemable.setDescription(description);
        return self();
    }
}
