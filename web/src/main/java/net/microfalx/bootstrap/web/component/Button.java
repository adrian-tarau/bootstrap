package net.microfalx.bootstrap.web.component;

/**
 * A button widget.
 */
public final class Button extends ActionableComponent<Button> {

    private boolean pressed;
    private Menu menu;

    /**
     * Returns whether the button is pressed.
     *
     * @return {@code true} if pressed, {@code false} otherwise
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Changes the press stated for this button.
     *
     * @param pressed {@code true} if pressed, {@code false} otherwise
     * @return self
     */
    public Button setPressed(boolean pressed) {
        this.pressed = pressed;
        return self();
    }

    /**
     * Returns the menu associated with the button.
     * <p>
     * If the button has a menu, it will be rendered as a dropdown.
     *
     * @return the menu, null if not a drop-down
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * Sets the menu associated with the button.
     *
     * @param menu the menu
     * @return self
     */
    public Button setMenu(Menu menu) {
        this.menu = menu;
        return self();
    }
}
