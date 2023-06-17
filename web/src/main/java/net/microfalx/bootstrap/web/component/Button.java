package net.microfalx.bootstrap.web.component;

/**
 * A button widget.
 */
public final class Button extends ActionableComponent<Button> {

    private boolean pressed;

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
}
