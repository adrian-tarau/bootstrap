package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An expression evaluator based on <a href="http://mvel.documentnode.com/">MVel</a>
 */
final class MvelTemplate<M, F extends Field<M>, ID> extends AbstractTemplate {

    private final CompiledTemplate compiledTemplate;

    MvelTemplate(Resource resource) throws IOException {
        super(resource);
        compiledTemplate = TemplateCompiler.compileTemplate(resource.getInputStream());
    }

    @Override
    public Type getType() {
        return Type.MVEL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T evaluate(TemplateContext context) {
        try {
            return (T) TemplateRuntime.execute(compiledTemplate, context.getModel(), context.toMap());
        } catch (Exception e) {
            throw new TemplateException("Failed to evaluate expression '" + getResource() + "'", e);
        }
    }

    @Override
    public void evaluate(TemplateContext context, OutputStream outputStream) throws IOException {
        try {
            TemplateRuntime.execute(compiledTemplate, context.getModel(), context.toMap(), outputStream);
        } catch (Exception e) {
            throw new TemplateException("Failed to evaluate expression '" + getResource() + "'", e);
        }
    }
}
