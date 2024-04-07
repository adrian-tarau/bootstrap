package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.mvel2.MVEL;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * An expression evaluator based on <a href="http://mvel.documentnode.com/">MVel</a>
 */
final class MvelTemplate<M, F extends Field<M>, ID> extends AbstractTemplate {

    private volatile CompiledTemplate template;
    private volatile Serializable expression;

    MvelTemplate(Resource resource) {
        super(resource);
    }

    @Override
    public Type getType() {
        return Type.MVEL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T doEvaluate(TemplateContext context) throws Exception {
        if (expression == null) {
            expression = MVEL.compileExpression(getResource().loadAsString());
        }
        return (T) MVEL.executeExpression(expression, context.getModel(), context.toMap());
    }

    @Override
    public void doEvaluate(TemplateContext context, OutputStream outputStream) throws Exception {
        if (template == null) {
            template = TemplateCompiler.compileTemplate(getResource().getInputStream());
        }
        TemplateRuntime.execute(template, context.getModel(), context.toMap(), outputStream);
    }
}
