package net.microfalx.bootstrap.template;

import net.microfalx.resource.Resource;

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
    public <T> T doEvaluate(TemplateContext context) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void doEvaluate(TemplateContext context, OutputStream outputStream) throws Exception {
        evaluate(context);
    }
}
