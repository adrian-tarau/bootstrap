package net.microfalx.bootstrap.template;

import net.microfalx.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A compiled template (expression) which can be evualted multiple types
 */
public interface Template {

    /**
     * Returns the template type.
     *
     * @return a non-null enum
     */
    Type getType();

    /**
     * Returns the resource of the template body (expression).
     *
     * @return a non-null instance
     */
    Resource getResource();

    /**
     * Evaluates the template (expression) and returns the result.
     *
     * @param context the evaluation context
     * @param <T>     the return type
     * @return the result
     */
    <T> T evaluate(TemplateContext context);

    /**
     * Evaluates the template and write the result to a writer.
     *
     * @param context      the evaluation
     * @param outputStream the output stream
     */
    void evaluate(TemplateContext context, OutputStream outputStream) throws IOException;

    /**
     * An enum which identifies the template engine.
     */
    enum Type {

        /**
         * An expression evaluator <a href="http://mvel.documentnode.com/">MVEL</a>
         */
        MVEL,

        /**
         * An expression evaluator <a href="https://www.thymeleaf.org/">Thymeleaf</a>
         */
        THYMELEAF

    }
}
