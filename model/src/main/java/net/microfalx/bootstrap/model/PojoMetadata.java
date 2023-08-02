package net.microfalx.bootstrap.model;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Base class for all POJO metadata.
 *
 * @param <M> the model
 */
public abstract class PojoMetadata<M, F extends PojoField<M>, ID> extends AbstractMetadata<M, F, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoMetadata.class);

    private static final String CLASS_FIELD_NAME = "class";

    @SuppressWarnings("unchecked")
    public PojoMetadata(Class<M> modelClass) {
        super(modelClass);
        extractFields((PojoMetadata<M, PojoField<M>, ID>) this, modelClass);
    }

    /**
     * Returns whether the property should be used.
     *
     * @param propertyDescriptor the property descriptor
     * @return {@code true} to accept the field, {@code false} otherwise
     */
    protected boolean accept(PropertyDescriptor propertyDescriptor) {
        return true;
    }

    /**
     * Creates a field implementation
     *
     * @param metadata the metadata
     * @param name     the field name
     * @param property the property name
     * @return a non-null instance
     */
    protected abstract F createField(PojoMetadata<M, PojoField<M>, ID> metadata, String name, String property);

    /**
     * Extracts Java Bean metadata and create fields as needed.
     *
     * @param metadata   the metadata instance
     * @param modelClass the model class
     */
    private void extractFields(PojoMetadata<M, PojoField<M>, ID> metadata, Class<?> modelClass) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(modelClass);
        int index = 0;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (!accept(propertyDescriptor) || CLASS_FIELD_NAME.equals(propertyDescriptor.getName())) continue;
            try {
                PojoField<M> field = createField(metadata, propertyDescriptor.getName(), propertyDescriptor.getName());
                field.setDataClass(propertyDescriptor.getPropertyType());
                field.setIndex(index++);
                field.update(getGetter(propertyDescriptor), getSetter(propertyDescriptor));
                Field jvmField = ReflectionUtils.findField(modelClass, propertyDescriptor.getName());
                if (jvmField != null) {
                    field.update(jvmField);
                } else {
                    field.update(propertyDescriptor.getReadMethod());
                }
                metadata.addField(field);
            } catch (Exception e) {
                throw new ModelException("Failed to process field '" + propertyDescriptor.getName()
                        + "' in " + ClassUtils.getName(modelClass), e);
            }
        }
    }

    private MethodHandle getGetter(PropertyDescriptor propertyDescriptor) {
        Method method = propertyDescriptor.getReadMethod();
        try {
            return method != null ? MethodHandles.lookup().unreflect(method) : null;
        } catch (IllegalAccessException e) {
            throw new ModelException("Failed to extract getter for " + propertyDescriptor, e);
        }
    }

    private MethodHandle getSetter(PropertyDescriptor propertyDescriptor) {
        Method method = propertyDescriptor.getWriteMethod();
        try {
            return method != null ? MethodHandles.lookup().unreflect(method) : null;
        } catch (IllegalAccessException e) {
            throw new ModelException("Failed to extract setter for " + propertyDescriptor, e);
        }
    }
}
