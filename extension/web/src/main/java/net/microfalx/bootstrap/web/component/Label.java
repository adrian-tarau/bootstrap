package net.microfalx.bootstrap.web.component;

/**
 * A simple text component.
 */
public class Label extends ItemableComponent<Label> {

    public static Label create(String text) {
        return new Label().setText(text);
    }

    private Label() {
    }

}
