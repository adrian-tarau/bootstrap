package net.microfalx.bootstrap.template;

import net.microfalx.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;

final class ThymeleafTemplate extends AbstractTemplate {

    ThymeleafTemplate(Resource resource) {
        super(resource);
    }

    @Override
    public Type getType() {
        return Type.THYMELEAF;
    }

    @Override
    public <T> T evaluate(TemplateContext context) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void evaluate(TemplateContext context, OutputStream outputStream) throws IOException {
        evaluate(context);
    }
}
