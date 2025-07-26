package net.microfalx.bootstrap.model;

import net.microfalx.lang.annotation.Order;

@Order(Order.AFTER)
public class PojoMetadataProvider<M, F extends Field<M>, ID> implements MetadataProvider<M, F, ID> {

    @Override
    public boolean supports(Class<M> modelClass) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Metadata<M, F, ID> getMetadata(Class<M> modelClass) {
        return (Metadata<M, F, ID>) new DefaultPojoMetadata<>(modelClass);
    }

    static class DefaultPojoField<M> extends PojoField<M> {

        public DefaultPojoField(PojoMetadata<M, PojoField<M>, ?> metadata, String name, String property) {
            super(metadata, name, property);
        }
    }

    static class DefaultPojoMetadata<M, ID> extends PojoMetadata<M, DefaultPojoField<M>, ID> {

        public DefaultPojoMetadata(Class<M> modelClass) {
            super(modelClass);
        }

        @Override
        protected DefaultPojoField<M> createField(PojoMetadata<M, PojoField<M>, ID> metadata, String name, String property) {
            return new DefaultPojoField<>(metadata, name, property);
        }
    }


}
