package net.microfalx.bootstrap.model;

import jakarta.persistence.Entity;
import net.microfalx.lang.Order;

import static net.microfalx.lang.AnnotationUtils.getAnnotation;

@Order
public class JpaMetadataProvider<M> implements MetadataProvider<M, JpaField<M>> {

    @Override
    public boolean supports(Class<M> modelClass) {
        return getAnnotation(modelClass, Entity.class) != null;
    }

    @Override
    public Metadata<M, JpaField<M>> getMetadata(Class<M> modelClass) {
        return new JpaMetadata<>(modelClass);
    }
}
