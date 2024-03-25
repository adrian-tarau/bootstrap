package net.microfalx.bootstrap.template;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service used to evaluate templates/expressions.
 */
@Service
public class TemplateService {

    @Autowired
    private TemplateProperties templateProperties;

    @Autowired
    private MetadataService metadataService;

    private final Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5))
            .maximumSize(5000).build();

    /**
     * Creates a template with an expression.
     *
     * @param type       the template type
     * @param expression the expression
     * @return a non-null instance
     */
    public Template getTemplate(Template.Type type, String expression) {
        requireNonNull(expression);
        return getTemplate(type, MemoryResource.create(expression));
    }

    /**
     * Creates a template with a template script.
     *
     * @param type     the template type
     * @param resource the resource containing the template body
     * @return a non-null instance
     */
    public Template getTemplate(Template.Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        try {
            return switch (type) {
                case MVEL -> new MvelTemplate<>(resource);
                case THYMELEAF -> new ThymeleafTemplate(resource);
            };
        } catch (Exception e) {
            throw new TemplateException("Failed to create template from " + resource, e);
        }
    }

    /**
     * Creates an empty template context.
     *
     * @return a non-null instance
     */
    public TemplateContext createContext() {
        return new DefaultTemplateContext<>(null, null, null);
    }

    /**
     * Creates a template context from a model.
     *
     * @param model the model
     * @param <M>   the model
     * @param <F>   the field type
     * @param <ID>  the identifier type
     * @return a non-null instance
     */
    public <M, F extends Field<M>, ID> TemplateContext createContext(M model) {
        return createContext(model, null);
    }

    /**
     * Creates a template context from a model and optional attributes.
     *
     * @param model      the model
     * @param attributes the attributes
     * @param <M>        the model
     * @param <F>        the field type
     * @param <ID>       the identifier type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <M, F extends Field<M>, ID> TemplateContext createContext(M model, Attributes<?> attributes) {
        requireNonNull(model);
        Metadata<M, Field<M>, Object> metadata = metadataService.getMetadata((Class<M>) model.getClass());
        return new DefaultTemplateContext<>(metadata, model, attributes);
    }

    /**
     * Creates a template context from attributes.
     *
     * @param attributes the attributes
     * @return a non-null instance
     */
    public TemplateContext createContext(Attributes<?> attributes) {
        requireNonNull(attributes);
        return new DefaultTemplateContext<>(null, null, attributes);
    }

}
