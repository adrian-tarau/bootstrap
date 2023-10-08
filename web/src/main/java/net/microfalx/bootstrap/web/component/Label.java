package net.microfalx.bootstrap.web.component;

/**
 * A simple text component.
 */
public class Label extends Component<Label> {

    private String text;

    public static Label create(String text) {
        return new Label().setText(text);
    }

    private Label() {
    }

    public String getText() {
        return text;
    }

    public Label setText(String text) {
        this.text = text;
        return this;
    }
}
