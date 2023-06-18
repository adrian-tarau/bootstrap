package net.microfalx.bootstrap.web.component.form;

import net.microfalx.bootstrap.web.component.Component;

/**
 * Base class for all fields.
 *
 * @param <F> the field type
 */
public abstract class BaseField<F extends BaseField<F>> extends Component<F> {

    private String name;
    private String text;
    private Object value;

    public final String getName() {
        return name;
    }

    public final F setName(String name) {
        this.name = name;
        return self();
    }

    public final String getText() {
        return text;
    }

    public final F setText(String text) {
        this.text = text;
        return self();
    }

    public final Object getValue() {
        return value;
    }

    public final F setValue(Object value) {
        this.value = value;
        return self();
    }
}
