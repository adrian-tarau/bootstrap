package net.microfalx.bootstrap.web.template.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all tools.
 */
public abstract class AbstractTool {

    protected final IContext templateContext;
    protected final ApplicationContext applicationContext;
    private ObjectMapper objectMapper;

    public AbstractTool(IContext templateContext, ApplicationContext applicationContext) {
        requireNonNull(templateContext);
        requireNonNull(applicationContext);
        this.templateContext = templateContext;
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the template context.
     *
     * @return a non-null instance
     */
    protected ITemplateContext getTemplateContext() {
        return (ITemplateContext) templateContext;
    }

    /**
     * Returns the web context.
     *
     * @return a non-null instance
     */
    protected final IWebContext getWebContext() {
        return (IWebContext) templateContext;
    }

    /**
     * Returns whether the current template is executed in the context of a web request.
     *
     * @return {@code true} if  a web context is available, {@code false} otherwise
     */
    protected final boolean hasWeb() {
        return templateContext instanceof IWebContext;
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
     * Returns the application context.
     *
     * @return a non-null instance
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
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

    /**
     * Creates an instance of a given class and injects the application context.
     *
     * @param clazz the class
     * @param <T>   the instance type
     * @return a new instance
     */
    public <T> T createInstance(Class<T> clazz) {
        T instance = ClassUtils.create(clazz);
        if (instance instanceof ApplicationContextAware aca) aca.setApplicationContext(applicationContext);
        return instance;
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            objectMapper = builder.build();
        }
        return objectMapper;
    }

}
