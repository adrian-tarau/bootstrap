package net.microfalx.bootstrap.web.template.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.lang.ExceptionUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all tools.
 */
public abstract class AbstractTool {

    protected final IContext context;
    private ObjectMapper objectMapper;

    public AbstractTool(IContext context) {
        requireNonNull(context);
        this.context = context;
    }

    /**
     * Returns the template context.
     *
     * @return a non-null instance
     */
    protected ITemplateContext getTemplateContext() {
        return (ITemplateContext) context;
    }

    /**
     * Returns the web context.
     *
     * @return a non-null instance
     */
    protected final IWebContext getWebContext() {
        return (IWebContext) context;
    }

    /**
     * Returns whether the current template is executed in the context of a web request.
     *
     * @return {@code true} if  a web context is available, {@code false} otherwise
     */
    protected final boolean hasWeb() {
        return context instanceof IWebContext;
    }

    /**
     * Returns the application path (servlet context).
     *
     * @return a non-null instance
     */
    public final String getApplicationPath() {
        return getWebContext().getExchange().getRequest().getApplicationPath();
    }

    /**
     * Writes an object as a JSON
     *
     * @param value the value
     * @return the value as JOSN
     */
    public final String toJson(Object value) {
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            objectMapper = builder.build();
        }
        return objectMapper;
    }

}
