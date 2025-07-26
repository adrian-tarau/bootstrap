package net.microfalx.bootstrap.template;

import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;

public class DefaultTemplateContext<M, F extends Field<M>, ID> extends AbstractTemplateContext<M, F, ID> {

    public DefaultTemplateContext(Metadata<M, F, ID> metadata, M model, Attributes<?> attributes) {
        super(metadata, model, attributes);
    }
}
