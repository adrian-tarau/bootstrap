package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.ModelException;

public class TemplateException extends ModelException {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
