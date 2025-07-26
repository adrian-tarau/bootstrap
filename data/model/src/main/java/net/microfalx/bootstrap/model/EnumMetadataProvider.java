package net.microfalx.bootstrap.model;

import net.microfalx.lang.annotation.Order;

@Order(Order.AFTER - 20)
public class EnumMetadataProvider<E extends Enum<E>> implements MetadataProvider<E, EnumField<E>, String> {

    @Override
    public boolean supports(Class<E> modelClass) {
        return modelClass.isEnum();
    }

    @Override
    public Metadata<E, EnumField<E>, String> getMetadata(Class<E> modelClass) {
        return new EnumMetadata<>(modelClass);
    }
}
