package net.microfalx.bootstrap.web.component;

/**
 * An interface for components used as items in a container.
 * <p/>
 * Each item has a text, icon and a display style associated with it.
 */
public interface Itemable<I extends Itemable<I>> {

    /**
     * Returns whether the itemable has text.
     *
     * @return <code>true</code> if it has text, <code>false</code> otherwise
     */
    boolean hasText();

    /**
     * Returns the text of this itemable.
     *
     * @return the text
     */
    String getText();

    /**
     * Sets the text associated with this itemable.
     *
     * @param text the text
     * @return self
     */
    I setText(String text);

    /**
     * Returns the description of this itemable.
     *
     * @return the text
     */
    String getDescription();

    /**
     * Sets the description associated with this itemable.
     *
     * @param description the description
     * @return self
     */
    I setDescription(String description);

    /**
     * Returns the icon (class) associated with this itemable.
     *
     * @return the icon path or null if there is no icon
     */
    String getIcon();

    /**
     * Sets the icon (class) associated with this itemable.
     *
     * @param icon the icon path
     * @return self
     */
    I setIcon(String icon);

    /**
     * Returns the display style of this itemable.
     *
     * @return a non-null enum
     */
    Style getStyle();

    /**
     * Changes the display style of this itemable
     *
     * @param style the style, cannot be null
     * @return self
     */
    I setStyle(Style style);

    /**
     * An enum used to indicate how the item is rendered
     */
    enum Style {

        /**
         * Only show the text of the item.
         */
        TEXT,

        /**
         * Only show the icon of the item.
         */
        ICON,

        /**
         * Show bot the icon and text of the item
         */
        BOTH
    }
}
