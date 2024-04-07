package net.microfalx.bootstrap.template;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for all templates
 */
public abstract class AbstractTemplate implements Template {

    private static final Metrics EXPRESSION = TemplateUtils.METRICS.withGroup("Evaluate Expression");
    private static final Metrics RESOURCE = TemplateUtils.METRICS.withGroup("Evaluate Resource");

    private final Resource resource;

    public AbstractTemplate(Resource resource) {
        ArgumentUtils.requireNonNull(resource);
        this.resource = resource;
    }

    @Override
    public final Resource getResource() {
        return resource;
    }

    @Override
    public final <T> T evaluate(TemplateContext context) {
        try {
            return EXPRESSION.timeCallable(resource.getName(), () -> doEvaluate(context));
        } catch (Exception e) {
            throw new TemplateException("Failed to evaluate expression '" + getResource() + "'", e);
        }
    }

    @Override
    public final void evaluate(TemplateContext context, OutputStream outputStream) throws IOException {
        try {
            RESOURCE.timeCallable(resource.getPath(), () -> {
                doEvaluate(context, outputStream);
                return null;
            });
        } catch (Exception e) {
            throw new TemplateException("Failed to evaluate expression '" + getResource() + "'", e);
        }
    }

    /**
     * Subclasses would implement this method to evaluate expressions.
     *
     * @param context the context
     * @param <T>     the return type
     * @return the return value
     * @throws Exception if the expression cannot be evaluated
     */
    protected abstract <T> T doEvaluate(TemplateContext context) throws Exception;

    /**
     * Subclasses would implement this method to evaluate a template and produce an output.
     *
     * @param context      the context
     * @param outputStream the output stream where to write the output     *
     * @throws Exception if the expression cannot be evaluated
     */
    protected abstract void doEvaluate(TemplateContext context, OutputStream outputStream) throws Exception;
}
