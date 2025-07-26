package net.microfalx.bootstrap.web.template.tools;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.MetadataService;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.IContext;

import java.util.Collections;
import java.util.List;

/**
 * Template utilities around models ({@link net.microfalx.bootstrap.model.Metadata}.
 */
@SuppressWarnings("unused")
public class ModelTool extends AbstractTool {

    private final MetadataService metadataService;

    public ModelTool(IContext templateContext, ApplicationContext applicationContext) {
        super(templateContext, applicationContext);
        this.metadataService = applicationContext.getBean(MetadataService.class);
    }

    /**
     * Describes the metadata for a model.
     *
     * @param model the model
     * @param <M>   the model type
     * @return the description
     */
    public <M> String describe(M model) {
        if (model == null) return "#NULL";
        try {
            ReflectionToStringBuilder builder = new ReflectionToStringBuilder(model, ToStringStyle.MULTI_LINE_STYLE);
            return builder.toString();
        } catch (Exception e) {
            return "#MODEL ERROR: " + e.getMessage();
        }
    }

    /**
     * Returns the fields behind a model.
     *
     * @param model the model
     * @param <M>   the model type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public <M> List<Field<M>> getFields(M model) {
        if (model == null) return Collections.emptyList();
        return metadataService.getMetadata((Class<M>) model.getClass()).getFields();
    }
}
