package net.microfalx.bootstrap.model;

import net.microfalx.lang.annotation.Order;

@Order(Order.AFTER)
public class PojoMetadataProvider<M, F extends Field<M>> implements MetadataProvider<M, F> {

    @Override
    public boolean supports(Class<M> modelClass) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Metadata<M, F> getMetadata(Class<M> modelClass) {
        return (Metadata<M, F>) new DefaultPojoMetadata<>(modelClass);
    }

    static class DefaultPojoField<M> extends PojoField<M> {

        public DefaultPojoField(PojoMetadata<M, PojoField<M>> metadata, String name, String property) {
            super(metadata, name, property);
        }
    }

    static class DefaultPojoMetadata<M> extends PojoMetadata<M, DefaultPojoField<M>> {

        public DefaultPojoMetadata(Class<M> modelClass) {
            super(modelClass);
        }

        @Override
        protected DefaultPojoField<M> createField(PojoMetadata<M, PojoField<M>> metadata, String name, String property) {
            return new DefaultPojoField<>(metadata, name, property);
        }
    }


}
