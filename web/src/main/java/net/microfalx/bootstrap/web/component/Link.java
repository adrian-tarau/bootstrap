package net.microfalx.bootstrap.web.component;

public class Link extends ActionableComponent<Link> {

    public static Link action(String text, String action) {
        return new Link().setText(text).setAction(action);
    }
}
